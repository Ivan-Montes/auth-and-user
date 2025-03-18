package dev.ime.domain.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Product {

	private Long productId;
	private String productName;
	private String productDescription;
	private Category category;
	private Set<Review> reviews = new HashSet<>();
	public Product() {
		super();
	}
	public Product(Long productId, String productName, String productDescription, Category category,
			Set<Review> reviews) {
		super();
		this.productId = productId;
		this.productName = productName;
		this.productDescription = productDescription;
		this.category = category;
		this.reviews = reviews;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getProductDescription() {
		return productDescription;
	}
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public Set<Review> getReviews() {
		return Collections.unmodifiableSet(reviews);
	}
	public void setReviews(Set<Review> reviews) {
		this.reviews = reviews;
	}
    public boolean addReview(Review review) {
        return reviews.add(review);
    }
    public boolean removeReview(Review review) {
        return reviews.remove(review);
    }
	@Override
	public int hashCode() {
		return Objects.hash(category, productDescription, productId, productName);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Product other = (Product) obj;
		return Objects.equals(category, other.category) && Objects.equals(productDescription, other.productDescription)
				&& Objects.equals(productId, other.productId) && Objects.equals(productName, other.productName);
	}
	@Override
	public String toString() {
		return "Product [productId=" + productId + ", productName=" + productName + ", productDescription="
				+ productDescription + ", category=" + category + "]";
	}

}
