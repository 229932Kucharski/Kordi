package pl.kucharski.Kordi.service.verification;

import pl.kucharski.Kordi.dto.UserDTO;
import pl.kucharski.Kordi.entity.User;

public interface VerificationService {
    String send(User user);
    String verify(UserDTO user, String token);
}
