package ru.itmo.infra.handler.usecase.admin.forceupdate;

import org.junit.jupiter.api.Test;
import ru.itmo.domain.dto.ForceUpdateDTO;

import static org.junit.jupiter.api.Assertions.*;

public class ForceUpdateDTOBuilderTest {

    @Test
    public void testToDTO_OnlyStatusField() throws Exception {
        String command = "/forceupdate 123456 \"Поток\" status=\"PRACTICE_APPROVED\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        ForceUpdateDTO dto = parser.toDTO();

        assertEquals(123456, dto.getIsu());
        assertEquals("Поток", dto.getEduStreamName());
        assertEquals("PRACTICE_APPROVED", dto.getStatus());
        assertNull(dto.getPracticePlace());
        assertNull(dto.getPracticeFormat());
        assertNull(dto.getCompanyINN());
        assertNull(dto.getCompanyName());
        assertNull(dto.getCompanyLeadFullName());
        assertNull(dto.getCompanyLeadPhone());
        assertNull(dto.getCompanyLeadEmail());
        assertNull(dto.getCompanyLeadJobTitle());
    }

    @Test
    public void testToDTO_OnlyPlaceField() throws Exception {
        String command = "/forceupdate 123456 \"Поток\" place=\"OTHER_COMPANY\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        ForceUpdateDTO dto = parser.toDTO();

        assertEquals("OTHER_COMPANY", dto.getPracticePlace());
        assertNull(dto.getStatus());
    }

    @Test
    public void testToDTO_MultipleFields() throws Exception {
        String command = "/forceupdate 123456 \"Поток\" status=\"REGISTERED\" place=\"ITMO_UNIVERSITY\" format=\"OFFLINE\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        ForceUpdateDTO dto = parser.toDTO();

        assertEquals(123456, dto.getIsu());
        assertEquals("Поток", dto.getEduStreamName());
        assertEquals("REGISTERED", dto.getStatus());
        assertEquals("ITMO_UNIVERSITY", dto.getPracticePlace());
        assertEquals("OFFLINE", dto.getPracticeFormat());
        assertNull(dto.getCompanyINN());
    }

    @Test
    public void testToDTO_AllCompanyFields() throws Exception {
        String command = "/forceupdate 123456 \"Поток\" inn=\"7801234567\" company=\"ООО Ромашка\" lead=\"Иванов И.И.\" phone=\"+79000000000\" email=\"boss@company.com\" title=\"Директор\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        ForceUpdateDTO dto = parser.toDTO();

        assertEquals("7801234567", dto.getCompanyINN());
        assertEquals("ООО Ромашка", dto.getCompanyName());
        assertEquals("Иванов И.И.", dto.getCompanyLeadFullName());
        assertEquals("+79000000000", dto.getCompanyLeadPhone());
        assertEquals("boss@company.com", dto.getCompanyLeadEmail());
        assertEquals("Директор", dto.getCompanyLeadJobTitle());
        assertNull(dto.getStatus());
        assertNull(dto.getPracticePlace());
    }

    @Test
    public void testToDTO_DryRunWithFields() throws Exception {
        String command = "/forceupdate --dry-run 123456 \"Поток\" status=\"PRACTICE_APPROVED\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        ForceUpdateDTO dto = parser.toDTO();

        assertTrue(parser.isDryRun());
        assertEquals("PRACTICE_APPROVED", dto.getStatus());
    }

    @Test
    public void testToDTO_AllFields() throws Exception {
        String command = "/forceupdate 123456 \"Поток\" status=\"APPLICATION_SIGNED\" place=\"OTHER_COMPANY\" format=\"ONLINE\" inn=\"1234567890\" company=\"Тест\" lead=\"Петров П.П.\" phone=\"+79999999999\" email=\"test@test.com\" title=\"Менеджер\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        ForceUpdateDTO dto = parser.toDTO();

        assertEquals(123456, dto.getIsu());
        assertEquals("Поток", dto.getEduStreamName());
        assertEquals("APPLICATION_SIGNED", dto.getStatus());
        assertEquals("OTHER_COMPANY", dto.getPracticePlace());
        assertEquals("ONLINE", dto.getPracticeFormat());
        assertEquals("1234567890", dto.getCompanyINN());
        assertEquals("Тест", dto.getCompanyName());
        assertEquals("Петров П.П.", dto.getCompanyLeadFullName());
        assertEquals("+79999999999", dto.getCompanyLeadPhone());
        assertEquals("test@test.com", dto.getCompanyLeadEmail());
        assertEquals("Менеджер", dto.getCompanyLeadJobTitle());
    }

    @Test
    public void testToDTO_RussianAliases() throws Exception {
        String command = "/forceupdate 123456 \"Поток\" статус=\"REGISTERED\" место=\"OTHER_COMPANY\" формат=\"OFFLINE\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        ForceUpdateDTO dto = parser.toDTO();

        assertEquals("REGISTERED", dto.getStatus());
        assertEquals("OTHER_COMPANY", dto.getPracticePlace());
        assertEquals("OFFLINE", dto.getPracticeFormat());
    }

    @Test
    public void testToDTO_MixedAliases() throws Exception {
        String command = "/forceupdate 123456 \"Поток\" status=\"REGISTERED\" mp=\"OTHER_COMPANY\" inn=\"1234567890\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        ForceUpdateDTO dto = parser.toDTO();

        assertEquals("REGISTERED", dto.getStatus());
        assertEquals("OTHER_COMPANY", dto.getPracticePlace());
        assertEquals("1234567890", dto.getCompanyINN());
    }

    @Test
    public void testToDTO_EmptyFields() throws Exception {
        String command = "/forceupdate 123456 \"Поток\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        ForceUpdateDTO dto = parser.toDTO();

        assertEquals(123456, dto.getIsu());
        assertEquals("Поток", dto.getEduStreamName());
        assertNull(dto.getStatus());
        assertNull(dto.getPracticePlace());
        assertNull(dto.getPracticeFormat());
        assertNull(dto.getCompanyINN());
        assertNull(dto.getCompanyName());
        assertNull(dto.getCompanyLeadFullName());
        assertNull(dto.getCompanyLeadPhone());
        assertNull(dto.getCompanyLeadEmail());
        assertNull(dto.getCompanyLeadJobTitle());
    }

    @Test
    public void testToDTO_VerifyAllFieldsAreNullableByDefault() {
        ForceUpdateDTO dto = ForceUpdateDTO.builder()
                .isu(123456)
                .eduStreamName("Test")
                .build();

        assertEquals(123456, dto.getIsu());
        assertEquals("Test", dto.getEduStreamName());
        assertNull(dto.getStatus());
        assertNull(dto.getPracticePlace());
        assertNull(dto.getPracticeFormat());
        assertNull(dto.getCompanyINN());
        assertNull(dto.getCompanyName());
        assertNull(dto.getCompanyLeadFullName());
        assertNull(dto.getCompanyLeadPhone());
        assertNull(dto.getCompanyLeadEmail());
        assertNull(dto.getCompanyLeadJobTitle());
    }
}