package prenotazione.medica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.model.Account;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse
{
    private Boolean isSuccess;
    private String message;
    private Account account;
}