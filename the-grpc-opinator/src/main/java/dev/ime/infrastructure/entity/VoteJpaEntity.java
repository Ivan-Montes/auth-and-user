package dev.ime.infrastructure.entity;


import dev.ime.common.constants.GlobalConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = GlobalConstants.VOT_CAT_DB)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class VoteJpaEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "vote_id")
	private Long voteId;

	@Column(name = "email", nullable = false, length = 100)
	private String email;

	@ManyToOne
	@JoinColumn(name = "review_id", nullable = false)
	private ReviewJpaEntity review;

	@Column(name = "useful", nullable = false)
	private boolean useful;
}
