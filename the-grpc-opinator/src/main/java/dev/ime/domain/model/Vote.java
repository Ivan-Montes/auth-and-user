package dev.ime.domain.model;

import java.util.Objects;

public class Vote {

	private Long voteId;
	private String email;
	private Review review;
	private boolean useful;
	
	public Vote() {
		super();
	}
	public Vote(Long voteId, String email, Review review, boolean useful) {
		super();
		this.voteId = voteId;
		this.email = email;
		this.review = review;
		this.useful = useful;
	}
	public Long getVoteId() {
		return voteId;
	}
	public void setVoteId(Long voteId) {
		this.voteId = voteId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Review getReview() {
		return review;
	}
	public void setReview(Review review) {
		this.review = review;
	}
	public boolean isUseful() {
		return useful;
	}
	public void setUseful(boolean useful) {
		this.useful = useful;
	}
	@Override
	public String toString() {
		return "Vote [voteId=" + voteId + ", email=" + email + ", review=" + (review != null ? review.getReviewId() : null) + ", useful=" + useful + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(email, review, useful, voteId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vote other = (Vote) obj;
		return Objects.equals(email, other.email) && Objects.equals(review, other.review) && useful == other.useful
				&& Objects.equals(voteId, other.voteId);
	}
}
