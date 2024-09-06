package bio.terra.pearl.compliance.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class PersonInScope {

    public PersonInScope(String email, String gitUser, String firstName, String lastName) {
        this.email = email;
        this.gitUser = gitUser;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public boolean matches(GithubAccount githubAccount) {
        return gitUser.equalsIgnoreCase(githubAccount.getAccountName());
    }

    private String email,
            gitUser, firstName, lastName;


    public String getFullName() {
        return firstName + " " + lastName;
    }
}
