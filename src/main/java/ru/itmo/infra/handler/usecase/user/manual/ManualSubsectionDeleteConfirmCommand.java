package ru.itmo.infra.handler.usecase.user.manual;

import lombok.NoArgsConstructor;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.storage.GuideRepository;

@NoArgsConstructor
public class ManualSubsectionDeleteConfirmCommand implements UserCommand {

    public static final String COMMAND_NAME = "/manual_sub_del_ok";

    @Override
    public MessageToUser execute(MessageDTO message) {
        var cd = new CallbackData(message.getText());
        if (!"subsectionId".equals(cd.getKey()) || cd.getValue() == null || cd.getValue().isBlank()) {
            return MessageToUser.builder()
                    .text("Некорректные данные.")
                    .needRewriting(true)
                    .build();
        }
        try {
            int subsectionId = Integer.parseInt(cd.getValue().trim());
            var subOpt = GuideRepository.findSubsectionById(subsectionId);
            if (subOpt.isEmpty()) {
                return MessageToUser.builder()
                        .text("Подраздел не найден.")
                        .needRewriting(true)
                        .build();
            }
            int sectionId = subOpt.get().getSectionId();
            GuideRepository.deleteSubsection(subsectionId);
            return ManualReorderView.build(sectionId);
        } catch (NumberFormatException e) {
            return MessageToUser.builder()
                    .text("Некорректный идентификатор.")
                    .needRewriting(true)
                    .build();
        } catch (BadRequestException e) {
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .needRewriting(true)
                    .build();
        } catch (InternalException e) {
            return MessageToUser.builder()
                    .text("Ошибка при удалении подраздела.")
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return true;
    }
}
