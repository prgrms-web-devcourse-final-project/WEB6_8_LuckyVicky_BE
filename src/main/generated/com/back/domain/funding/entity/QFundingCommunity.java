package com.back.domain.funding.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFundingCommunity is a Querydsl query type for FundingCommunity
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFundingCommunity extends EntityPathBase<FundingCommunity> {

    private static final long serialVersionUID = -294601450L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFundingCommunity fundingCommunity = new QFundingCommunity("fundingCommunity");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final com.back.domain.user.entity.QUser author;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final BooleanPath deleted = createBoolean("deleted");

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final QFunding funding;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public QFundingCommunity(String variable) {
        this(FundingCommunity.class, forVariable(variable), INITS);
    }

    public QFundingCommunity(Path<? extends FundingCommunity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFundingCommunity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFundingCommunity(PathMetadata metadata, PathInits inits) {
        this(FundingCommunity.class, metadata, inits);
    }

    public QFundingCommunity(Class<? extends FundingCommunity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new com.back.domain.user.entity.QUser(forProperty("author")) : null;
        this.funding = inits.isInitialized("funding") ? new QFunding(forProperty("funding"), inits.get("funding")) : null;
    }

}

