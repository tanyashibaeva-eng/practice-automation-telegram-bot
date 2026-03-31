package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.domain.model.CompanyApprovalRequest;
import ru.itmo.domain.type.CompanyApprovalRequestStatus;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.exception.InternalException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class CompanyApprovalRequestRepository {

    public static long save(CompanyApprovalRequest request) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                INSERT INTO company_approval_request (
                    student_chat_id,
                    edu_stream_name,
                    inn,
                    company_name,
                    company_address,
                    practice_format,
                    company_lead_fullname,
                    company_lead_phone,
                    company_lead_email,
                    company_lead_job_title,
                    requires_spb_office_approval,
                    status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id;
                """
        )) {
            fillDraft(statement, request);
            statement.setBoolean(11, request.isRequiresSpbOfficeApproval());
            statement.setObject(12, request.getStatus(), Types.OTHER);

            var rs = statement.executeQuery();
            rs.next();
            return rs.getLong("id");
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updatePendingDraft(CompanyApprovalRequest request) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                UPDATE company_approval_request
                SET inn = ?,
                    company_name = ?,
                    company_address = ?,
                    practice_format = ?,
                    company_lead_fullname = ?,
                    company_lead_phone = ?,
                    company_lead_email = ?,
                    company_lead_job_title = ?,
                    requires_spb_office_approval = ?,
                    created_at = now()
                WHERE id = ? AND status = 'PENDING';
                """
        )) {
            statement.setLong(1, request.getInn());
            statement.setString(2, request.getCompanyName());
            statement.setString(3, request.getCompanyAddress());
            statement.setObject(4, request.getPracticeFormat(), Types.OTHER);
            statement.setString(5, request.getCompanyLeadFullName());
            statement.setString(6, request.getCompanyLeadPhone());
            statement.setString(7, request.getCompanyLeadEmail());
            statement.setString(8, request.getCompanyLeadJobTitle());
            statement.setBoolean(9, request.isRequiresSpbOfficeApproval());
            statement.setLong(10, request.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<CompanyApprovalRequest> findPendingByStudentChatIdAndEduStreamName(long studentChatId, String eduStreamName) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT * FROM company_approval_request
                WHERE student_chat_id = ? AND edu_stream_name = ? AND status = 'PENDING';
                """
        )) {
            statement.setLong(1, studentChatId);
            statement.setString(2, eduStreamName);
            var rs = statement.executeQuery();
            return mapToRequestOptional(rs);
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<CompanyApprovalRequest> findPendingById(long id) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT * FROM company_approval_request
                WHERE id = ? AND status = 'PENDING';
                """
        )) {
            statement.setLong(1, id);
            var rs = statement.executeQuery();
            return mapToRequestOptional(rs);
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<CompanyApprovalRequest> findAllPending() throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT * FROM company_approval_request
                WHERE status = 'PENDING'
                ORDER BY created_at ASC;
                """
        )) {
            var rs = statement.executeQuery();
            return mapToRequestList(rs);
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateStatus(long id, CompanyApprovalRequestStatus status, Long processedByChatId) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                UPDATE company_approval_request
                SET status = ?,
                    processed_by_chat_id = ?,
                    processed_at = now()
                WHERE id = ? AND status = 'PENDING';
                """
        )) {
            statement.setObject(1, status, Types.OTHER);
            if (processedByChatId == null) {
                statement.setNull(2, Types.BIGINT);
            } else {
                statement.setLong(2, processedByChatId);
            }
            statement.setLong(3, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private static void fillDraft(java.sql.PreparedStatement statement, CompanyApprovalRequest request) throws SQLException {
        statement.setLong(1, request.getStudentChatId());
        statement.setString(2, request.getEduStreamName());
        statement.setLong(3, request.getInn());
        statement.setString(4, request.getCompanyName());
        statement.setString(5, request.getCompanyAddress());
        statement.setObject(6, request.getPracticeFormat(), Types.OTHER);
        statement.setString(7, request.getCompanyLeadFullName());
        statement.setString(8, request.getCompanyLeadPhone());
        statement.setString(9, request.getCompanyLeadEmail());
        statement.setString(10, request.getCompanyLeadJobTitle());
    }

    private static Optional<CompanyApprovalRequest> mapToRequestOptional(ResultSet rs) throws SQLException {
        var request = mapToRequest(rs);
        return request == null ? Optional.empty() : Optional.of(request);
    }

    private static List<CompanyApprovalRequest> mapToRequestList(ResultSet rs) throws SQLException {
        var result = new ArrayList<CompanyApprovalRequest>();
        var request = mapToRequest(rs);
        while (request != null) {
            result.add(request);
            request = mapToRequest(rs);
        }
        return result;
    }

    private static CompanyApprovalRequest mapToRequest(ResultSet rs) throws SQLException {
        if (!rs.next()) {
            return null;
        }

        Long processedByChatId = rs.getLong("processed_by_chat_id");
        if (rs.wasNull()) {
            processedByChatId = null;
        }

        return CompanyApprovalRequest.builder()
                .id(rs.getLong("id"))
                .studentChatId(rs.getLong("student_chat_id"))
                .eduStreamName(rs.getString("edu_stream_name"))
                .inn(rs.getLong("inn"))
                .companyName(rs.getString("company_name"))
                .companyAddress(rs.getString("company_address"))
                .practiceFormat(PracticeFormat.valueOfIgnoreCase(rs.getString("practice_format")))
                .companyLeadFullName(rs.getString("company_lead_fullname"))
                .companyLeadPhone(rs.getString("company_lead_phone"))
                .companyLeadEmail(rs.getString("company_lead_email"))
                .companyLeadJobTitle(rs.getString("company_lead_job_title"))
                .requiresSpbOfficeApproval(rs.getBoolean("requires_spb_office_approval"))
                .status(CompanyApprovalRequestStatus.valueOfIgnoreCase(rs.getString("status")))
                .processedByChatId(processedByChatId)
                .createdAt(rs.getTimestamp("created_at"))
                .processedAt(rs.getTimestamp("processed_at"))
                .build();
    }

    private static InternalException handleAndWrapSQLException(SQLException ex) {
        log.severe("Ошибка во время выполнения SQL запроса: " + ex.getMessage());
        return new InternalException("Что-то пошло не так", ex.getCause());
    }
}
