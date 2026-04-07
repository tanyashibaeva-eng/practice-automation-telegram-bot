package ru.itmo.infra.handler.usecase.user.studentregistration;

import lombok.SneakyThrows;
import ru.itmo.application.GuideService;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.SimpleMarkdownToTelegramHtml;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.storage.GuideRepository;

public class StudentRegistrationStartCommand implements UserCommand {

    private static final String REGISTRATION_GUIDE_SECTION_SLUG = "registration_instruction";

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.setNextCommand(message.getChatId(), new StudentRegistrationProcessStreamCommand());

        String instructionHtml = null;
        try {
            var sectionOpt = GuideRepository.findActiveSectionBySlug(REGISTRATION_GUIDE_SECTION_SLUG);
            if (sectionOpt.isPresent()) {
                var firstSubOpt = GuideRepository.findFirstSubsectionBySectionId(sectionOpt.get().getId());
                if (firstSubOpt.isPresent()) {
                    instructionHtml = GuideService.previewSubsectionHtml(firstSubOpt.get(),
                            firstSubOpt.get().getBody() == null ? "" : firstSubOpt.get().getBody());
                }
            }
        } catch (Exception ignored) {
            // при ошибках базы — используем дефолт ниже
        }

        if (instructionHtml == null) {
            instructionHtml = SimpleMarkdownToTelegramHtml.convert(
                    "При регистрации, требуется указать\n\n" +
                            "* название потока  \n" +
                            "* номер ИСУ\n\n" +
                            "Введите название вашего потока");
        }

        String text = instructionHtml;
        return MessageToUser.builder()
                .text(text)
                .parseMode("HTML")
                .keyboardMarkup(getReturnToStartMarkup())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/register";
    }

    @Override
    public String getDescription() {
        return "Зарегистрироваться";
    }

    @Override
    public String getDisplayName() {
        return "Зарегистрироваться";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return status == StudentStatus.NOT_REGISTERED;
    }
}
