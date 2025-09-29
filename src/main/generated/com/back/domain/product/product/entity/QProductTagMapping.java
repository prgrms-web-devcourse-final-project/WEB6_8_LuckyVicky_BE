package com.back.domain.product.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductTagMapping is a Querydsl query type for ProductTagMapping
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductTagMapping extends EntityPathBase<ProductTagMapping> {

    private static final long serialVersionUID = -310969562L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductTagMapping productTagMapping = new QProductTagMapping("productTagMapping");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final QProduct product;

    public final com.back.domain.product.tag.entity.QTag tag;

    public QProductTagMapping(String variable) {
        this(ProductTagMapping.class, forVariable(variable), INITS);
    }

    public QProductTagMapping(Path<? extends ProductTagMapping> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductTagMapping(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductTagMapping(PathMetadata metadata, PathInits inits) {
        this(ProductTagMapping.class, metadata, inits);
    }

    public QProductTagMapping(Class<? extends ProductTagMapping> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product"), inits.get("product")) : null;
        this.tag = inits.isInitialized("tag") ? new com.back.domain.product.tag.entity.QTag(forProperty("tag")) : null;
    }

}

