package dev.ime.domain.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Review {

	private Long reviewId;
	private String email;
	private Product product;
	private String reviewText;
	private int rating;
	private Set<Vote> votes = new HashSet<>();
	public Review() {
		super();
	}
	public Review(Long reviewId, String email, Product product, String reviewText, int rating, Set<Vote> votes) {
		super();
		this.reviewId = reviewId;
		this.email = email;
		this.product = product;
		this.reviewText = reviewText;
		this.rating = rating;
		this.votes = votes;
	}
	public Long getReviewId() {
		return reviewId;
	}
	public void setReviewId(Long reviewId) {
		this.reviewId = reviewId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public String getReviewText() {
		return reviewText;
	}
	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public Set<Vote> getVotes() {
		return Collections.unmodifiableSet(votes);
	}
	public void setVotes(Set<Vote> votes) {
		this.votes = votes;
	}
	public boolean addVote(Vote vote) {
        return votes.add(vote);
    }
    public boolean removeVote(Vote vote) {
        return votes.remove(vote);
    }
	@Override
	public int hashCode() {
		return Objects.hash(email, product, rating, reviewId, reviewText);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Review other = (Review) obj;
		return Objects.equals(email, other.email) && Objects.equals(product, other.product) && rating == other.rating
				&& Objects.equals(reviewId, other.reviewId) && Objects.equals(reviewText, other.reviewText);
	}
	@Override
	public String toString() {
		return "Review [reviewId=" + reviewId + ", email=" + email + ", product=" + (product != null ? product.getProductId() : null) + ", reviewText="
				+ reviewText + ", rating=" + rating + "]";
	}

}
