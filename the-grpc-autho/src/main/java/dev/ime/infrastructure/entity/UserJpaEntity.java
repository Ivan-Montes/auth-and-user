package dev.ime.infrastructure.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import dev.ime.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table( name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserJpaEntity implements UserDetails {
	
	private static final long serialVersionUID = -4695165278124465518L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;
	
	@Column(nullable = false, unique = true)
	private String email;
	
	@Column(nullable = false)
	@ToString.Exclude
	private String password;

    @Enumerated(EnumType.STRING) 
    private Role role;
    
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		return List.of(new SimpleGrantedAuthority((role.name()))); 
		
	}
	
	@Override
	public String getUsername() {
		
		return email;
		
	}
	
}
