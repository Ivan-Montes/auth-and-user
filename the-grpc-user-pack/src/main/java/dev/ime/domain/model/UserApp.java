package dev.ime.domain.model;

import java.util.Objects;

public class UserApp {

	private Long userAppId;
	private String email;
	private String name;
	private String lastname;

	public UserApp() {
		super();
	}

	public UserApp(Long userAppId, String email, String name, String lastname) {
		super();
		this.userAppId = userAppId;
		this.email = email;
		this.name = name;
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public String getLastname() {
		return lastname;
	}

	public String getName() {
		return name;
	}

	public Long getUserAppId() {
		return userAppId;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUserAppId(Long userAppId) {
		this.userAppId = userAppId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, lastname, name, userAppId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserApp other = (UserApp) obj;
		return Objects.equals(email, other.email) && Objects.equals(lastname, other.lastname)
				&& Objects.equals(name, other.name) && Objects.equals(userAppId, other.userAppId);
	}

	@Override
	public String toString() {
		return "UserApp [userAppId=" + userAppId + ", email=" + email + ", name=" + name + ", lastname=" + lastname
				+ "]";
	}

}
