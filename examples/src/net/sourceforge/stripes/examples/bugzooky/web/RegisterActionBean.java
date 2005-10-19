package net.sourceforge.stripes.examples.bugzooky.web;

import net.sourceforge.stripes.examples.bugzooky.biz.Person;
import net.sourceforge.stripes.examples.bugzooky.biz.PersonManager;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.Validatable;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.LocalizableError;

/**
 * ActionBean that handles the registration of new users.
 *
 * @author Tim Fennell
 */
@UrlBinding("/bugzooky/Register.action")
public class RegisterActionBean extends BugzookyActionBean implements Validatable {
    private Person user;
    private String confirmPassword;

    /** The user being registered. */
    @ValidateNestedProperties({
        @Validate(field="username", required=true, minlength=5, maxlength=20),
        @Validate(field="password", required=true, minlength=5, maxlength=20),
        @Validate(field="firstName", required=true, maxlength=50),
        @Validate(field="lastName", required=true, maxlength=50)
    })
    public void setUser(Person user) { this.user = user; }

    /** The user being registered. */
    public Person getUser() { return user; }

    /** The 2nd/confirmation password entered by the user. */
    @Validate(required=true, minlength=5, maxlength=20)
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /** The 2nd/confirmation password entered by the user. */
    public String getConfirmPassword() { return confirmPassword; }

    /**
     * Validates that the two passwords entered match each other, and that the
     * username entered is not already taken in the system.
     */
    public void validate(ValidationErrors errors) {
        if (!this.user.getPassword().equals(this.confirmPassword)) {
            errors.add("confirmPassword",
                       new LocalizableError("/bugzooky/Register.action.passwordsDontMatch"));
        }

        if ( new PersonManager().getPerson(this.user.getUsername()) != null ) {
            errors.add("user.username",
                       new LocalizableError("/bugzooky/Register.action.usernameTaken",
                                            this.user.getUsername()));
        }
    }

    /**
     * Registers a new user, logs them in, and redirects them to the bug list page.
     */
    @DefaultHandler @HandlesEvent("Register")
    public Resolution registerUser() {
        PersonManager pm = new PersonManager();
        pm.saveOrUpdate(this.user);
        getContext().setUser(this.user);

        return new RedirectResolution("/bugzooky/BugList.jsp");
    }
}
