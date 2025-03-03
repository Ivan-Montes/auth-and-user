DROP DATABASE IF EXISTS opinatorSqlDb;
CREATE DATABASE opinatorSqlDb;

DROP TABLE IF EXISTS categories cascade;
DROP TABLE IF EXISTS products cascade;
DROP TABLE IF EXISTS reviews cascade;
DROP TABLE IF EXISTS votes cascade;

CREATE TABLE categories (
	category_id SERIAL PRIMARY KEY,
    category_name varchar(100) not null unique 
	);
CREATE TABLE products ( 
	product_id SERIAL PRIMARY KEY,
	category_id bigint not null,
	product_name varchar(100) not null unique, 
	product_description varchar(500) 
	);
CREATE TABLE reviews (
	review_id SERIAL PRIMARY KEY,
	rating integer not null, 
	product_id bigint not null, 
	email varchar(100) not null, 
	review_text varchar(1000) not null 
	);
CREATE TABLE votes (
	vote_id SERIAL PRIMARY KEY,
	useful boolean not null, 
	review_id bigint not null, 
	email varchar(100) not null
	);
	
ALTER TABLE products 
ADD CONSTRAINT FK_products_categories 
FOREIGN KEY (category_id) 
REFERENCES categories (category_id);

ALTER TABLE reviews 
ADD CONSTRAINT FK_reviews_products 
FOREIGN KEY (product_id) 
REFERENCES products (product_id);

ALTER TABLE votes 
ADD CONSTRAINT FK_votes_reviews 
FOREIGN KEY (review_id) 
REFERENCES reviews (review_id);
	
	