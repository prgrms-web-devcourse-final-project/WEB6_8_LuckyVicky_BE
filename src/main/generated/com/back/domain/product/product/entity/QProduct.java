package com.back.domain.product.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProduct is a Querydsl query type for Product
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProduct extends EntityPathBase<Product> {

    private static final long serialVersionUID = -1271921230L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProduct product = new QProduct("product");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final ListPath<AdditionalProduct, QAdditionalProduct> additionalProducts = this.<AdditionalProduct, QAdditionalProduct>createList("additionalProducts", AdditionalProduct.class, QAdditionalProduct.class, PathInits.DIRECT2);

    public final NumberPath<Integer> additionalShippingCharge = createNumber("additionalShippingCharge", Integer.class);

    public final StringPath brandName = createString("brandName");

    public final BooleanPath bundleShippingAvailable = createBoolean("bundleShippingAvailable");

    public final com.back.domain.product.category.entity.QCategory category;

    public final BooleanPath certification = createBoolean("certification");

    public final NumberPath<Integer> conditionalFreeAmount = createNumber("conditionalFreeAmount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final NumberPath<Integer> deliveryCharge = createNumber("deliveryCharge", Integer.class);

    public final EnumPath<DeliveryType> deliveryType = createEnum("deliveryType", DeliveryType.class);

    public final StringPath description = createString("description");

    public final NumberPath<Integer> discountRate = createNumber("discountRate", Integer.class);

    public final EnumPath<DisplayStatus> displayStatus = createEnum("displayStatus", DisplayStatus.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final ListPath<ProductImage, QProductImage> images = this.<ProductImage, QProductImage>createList("images", ProductImage.class, QProductImage.class, PathInits.DIRECT2);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final BooleanPath isPlanned = createBoolean("isPlanned");

    public final BooleanPath isRestock = createBoolean("isRestock");

    public final StringPath material = createString("material");

    public final NumberPath<Integer> maxQuantity = createNumber("maxQuantity", Integer.class);

    public final NumberPath<Integer> minQuantity = createNumber("minQuantity", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final StringPath name = createString("name");

    public final ListPath<Option, QOption> options = this.<Option, QOption>createList("options", Option.class, QOption.class, PathInits.DIRECT2);

    public final StringPath origin = createString("origin");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final StringPath productModelName = createString("productModelName");

    public final SetPath<ProductTagMapping, QProductTagMapping> productTags = this.<ProductTagMapping, QProductTagMapping>createSet("productTags", ProductTagMapping.class, QProductTagMapping.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> sellingEndDate = createDateTime("sellingEndDate", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> sellingStartDate = createDateTime("sellingStartDate", java.time.LocalDateTime.class);

    public final EnumPath<SellingStatus> sellingStatus = createEnum("sellingStatus", SellingStatus.class);

    public final StringPath size = createString("size");

    public final NumberPath<Integer> stock = createNumber("stock", Integer.class);

    public final com.back.domain.user.entity.QUser user;

    public QProduct(String variable) {
        this(Product.class, forVariable(variable), INITS);
    }

    public QProduct(Path<? extends Product> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProduct(PathMetadata metadata, PathInits inits) {
        this(Product.class, metadata, inits);
    }

    public QProduct(Class<? extends Product> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.back.domain.product.category.entity.QCategory(forProperty("category"), inits.get("category")) : null;
        this.user = inits.isInitialized("user") ? new com.back.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

