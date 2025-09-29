package com.back.domain.product.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdditionalProduct is a Querydsl query type for AdditionalProduct
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdditionalProduct extends EntityPathBase<AdditionalProduct> {

    private static final long serialVersionUID = 1411163083L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdditionalProduct additionalProduct = new QAdditionalProduct("additionalProduct");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final StringPath additionalName = createString("additionalName");

    public final NumberPath<Integer> additionalPrice = createNumber("additionalPrice", Integer.class);

    public final NumberPath<Integer> additionalStock = createNumber("additionalStock", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final QProduct product;

    public QAdditionalProduct(String variable) {
        this(AdditionalProduct.class, forVariable(variable), INITS);
    }

    public QAdditionalProduct(Path<? extends AdditionalProduct> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdditionalProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdditionalProduct(PathMetadata metadata, PathInits inits) {
        this(AdditionalProduct.class, metadata, inits);
    }

    public QAdditionalProduct(Class<? extends AdditionalProduct> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product"), inits.get("product")) : null;
    }

}

