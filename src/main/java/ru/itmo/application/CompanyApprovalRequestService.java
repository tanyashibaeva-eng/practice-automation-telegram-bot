package ru.itmo.application;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.bot.CallbackData;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.model.CompanyApprovalRequest;
import ru.itmo.domain.type.CompanyApprovalRequestStatus;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.admin.companyapproval.ListCompanyApprovalRequestsCommand;
import ru.itmo.infra.notification.Notification;
import ru.itmo.infra.notification.Notifier;
import ru.itmo.infra.storage.CompanyApprovalRequestRepository;

import java.util.List;

public class CompanyApprovalRequestService {

    private CompanyApprovalRequestService() {
    }

    public static long submitRequest(CompanyInfoUpdateArgs args) throws InternalException, BadRequestException {
        var eduStreamName = StudentService.findActiveEduStreamNameByChatId(args.getChatId())
                .orElseThrow(() -> new BadRequestException("Студент не найден в активном потоке"));

        var request = CompanyApprovalRequest.builder()
                .studentChatId(args.getChatId())
                .eduStreamName(eduStreamName)
                .inn(args.getInn())
                .companyName(args.getCompanyName())
                .companyAddress(args.getCompanyAddress())
                .practiceFormat(args.getPracticeFormat())
                .companyLeadFullName(args.getCompanyLeadFullName())
                .companyLeadPhone(args.getCompanyLeadPhone())
                .companyLeadEmail(args.getCompanyLeadEmail())
                .companyLeadJobTitle(args.getCompanyLeadJobTitle())
                .requiresSpbOfficeApproval(args.isRequiresSpbOfficeApproval())
                .status(CompanyApprovalRequestStatus.PENDING)
                .build();

        var existingRequestOpt = CompanyApprovalRequestRepository.findPendingByStudentChatIdAndEduStreamName(
                args.getChatId(),
                eduStreamName
        );

        long requestId;
        if (existingRequestOpt.isPresent()) {
            request.setId(existingRequestOpt.get().getId());
            if (!CompanyApprovalRequestRepository.updatePendingDraft(request)) {
                throw new InternalException("Не удалось обновить черновик заявки");
            }
            requestId = request.getId();
        } else {
            requestId = CompanyApprovalRequestRepository.save(request);
            request.setId(requestId);
        }

        if (!StudentService.updateStatusByChatIdAndEduStreamName(
                args.getChatId(),
                eduStreamName,
                StudentStatus.COMPANY_INFO_WAITING_APPROVAL
        )) {
            throw new InternalException("Не удалось обновить статус студента после отправки заявки");
        }

        notifyAdminsAboutPendingRequest(request);
        return requestId;
    }

    public static List<CompanyApprovalRequest> getPendingRequests() throws InternalException {
        return CompanyApprovalRequestRepository.findAllPending();
    }

    public static CompanyApprovalRequest getPendingRequestOrThrow(long requestId) throws InternalException, BadRequestException {
        return CompanyApprovalRequestRepository.findPendingById(requestId)
                .orElseThrow(() -> new BadRequestException("Заявка с id %d не найдена".formatted(requestId)));
    }

    public static void approveRequest(long requestId, long adminChatId) throws InternalException, BadRequestException {
        var request = getPendingRequestOrThrow(requestId);

        var updateArgs = CompanyInfoUpdateArgs.builder()
                .chatId(request.getStudentChatId())
                .inn(request.getInn())
                .companyName(request.getCompanyName())
                .companyAddress(request.getCompanyAddress())
                .practiceFormat(request.getPracticeFormat())
                .companyLeadFullName(request.getCompanyLeadFullName())
                .companyLeadPhone(request.getCompanyLeadPhone())
                .companyLeadEmail(request.getCompanyLeadEmail())
                .companyLeadJobTitle(request.getCompanyLeadJobTitle())
                .build();

        if (!StudentService.updateCompanyInfo(updateArgs, request.getEduStreamName())) {
            throw new InternalException("Не удалось сохранить данные компании в основной системе");
        }

        ApprovedCompanyRegistryService.saveApprovedCompany(request);

        if (!CompanyApprovalRequestRepository.updateStatus(requestId, CompanyApprovalRequestStatus.APPROVED, adminChatId)) {
            throw new InternalException("Не удалось обновить статус заявки");
        }

        Notifier.notifyAsync(Notification.builder()
                .chatId(request.getStudentChatId())
                .text(request.isRequiresSpbOfficeApproval()
                        ? "Администратор подтвердил наличие офиса компании в Санкт-Петербурге. Данные отправлены на проверку преподавателю."
                        : "Данные о компании были подтверждены администратором и отправлены на проверку преподавателю.")
                .build());
    }

    public static void rejectRequest(long requestId, long adminChatId) throws InternalException, BadRequestException {
        var request = getPendingRequestOrThrow(requestId);

        if (!CompanyApprovalRequestRepository.updateStatus(requestId, CompanyApprovalRequestStatus.REJECTED, adminChatId)) {
            throw new InternalException("Не удалось обновить статус заявки");
        }

        Notifier.notifyAsync(Notification.builder()
                .chatId(request.getStudentChatId())
                .text(request.isRequiresSpbOfficeApproval()
                        ? "Администратор не подтвердил наличие офиса компании %s в Санкт-Петербурге. Проверьте ИНН и адрес офиса и отправьте заявку заново."
                        .formatted(request.getCompanyName())
                        : "Заявка на новую компанию была отклонена администратором. Проверьте данные и отправьте их заново.")
                .build());
    }

    private static void notifyAdminsAboutPendingRequest(CompanyApprovalRequest request) throws InternalException, BadRequestException {
        var pendingRequests = CompanyApprovalRequestRepository.findAllPending();
        if (pendingRequests.isEmpty()) {
            return;
        }

        var studentName = StudentService.findStudentByChatIdAndEduStreamName(request.getStudentChatId(), request.getEduStreamName())
                .map(student -> student.getFullName())
                .orElse("Неизвестный студент");

        var keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("Открыть список")
                                .callbackData(CallbackData.builder()
                                        .command(new ListCompanyApprovalRequestsCommand().getName())
                                        .build()
                                        .toString())
                                .build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("Подтвердить #" + request.getId())
                                .callbackData(CallbackData.builder()
                                        .command("/approve_company_request_" + request.getId())
                                        .build()
                                        .toString())
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("Отклонить #" + request.getId())
                                .callbackData(CallbackData.builder()
                                        .command("/reject_company_request_" + request.getId())
                                        .build()
                                        .toString())
                                .build()
                ))
                .build();

        for (var admin : TelegramUserService.getAllNotBannedAdmins()) {
            Notifier.notifyAsync(Notification.builder()
                    .chatId(admin.getChatId())
                    .text("""
                            Новая заявка #%d на подтверждение %s.
                            Студент: %s
                            Компания: %s
                            ИНН: %d
                            Сейчас в очереди %d заявок.
                            """.formatted(
                            request.getId(),
                            request.isRequiresSpbOfficeApproval()
                                    ? "офиса компании в Санкт-Петербурге"
                                    : "компании",
                            studentName,
                            request.getCompanyName(),
                            request.getInn(),
                            pendingRequests.size()
                    ).trim())
                    .keyboardMarkup(keyboard)
                    .build());
        }
    }
}
