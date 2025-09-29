package com.back.domain.product.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductImage is a Querydsl query type for ProductImage
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductImage extends EntityPathBase<ProductImage> {

    private static final long serialVersionUID = 637629129L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductImage productImage = new QProductImage("productImage");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final EnumPath<com.back.global.s3.FileType> fileType = createEnum("fileType", com.back.global.s3.FileType.class);

    public final StringPath fileUrl = createString("fileUrl");

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final StringPath originalFilename = createString("originalFilename");

    public final QProduct product;

    public final StringPath s3Key = createString("s3Key");

    public QProductImage(String variable) {
        this(ProductImage.class, forVariable(variable), INITS);
    }

    public QProductImage(Path<? extends ProductImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductImage(PathMetadata metadata, PathInits inits) {
        this(ProductImage.class, metadata, inits);
    }

    public QProductImage(Class<? extends ProductImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product"), inits.get("product")) : null;
    }

}

