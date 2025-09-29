package com.back.domain.funding.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFunding is a Querydsl query type for Funding
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFunding extends EntityPathBase<Funding> {

    private static final long serialVersionUID = -2117011437L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFunding funding = new QFunding("funding");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final NumberPath<Long> collectedAmount = createNumber("collectedAmount", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final StringPath description = createString("description");

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final ListPath<FundingImage, QFundingImage> images = this.<FundingImage, QFundingImage>createList("images", FundingImage.class, QFundingImage.class, PathInits.DIRECT2);

    public final StringPath imageUrl = createString("imageUrl");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final ListPath<FundingOption, QFundingOption> options = this.<FundingOption, QFundingOption>createList("options", FundingOption.class, QFundingOption.class, PathInits.DIRECT2);

    public final NumberPath<Integer> participantCount = createNumber("participantCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final EnumPath<FundingStatus> status = createEnum("status", FundingStatus.class);

    public final NumberPath<Long> targetAmount = createNumber("targetAmount", Long.class);

    public final StringPath title = createString("title");

    public final com.back.domain.user.entity.QUser user;

    public QFunding(String variable) {
        this(Funding.class, forVariable(variable), INITS);
    }

    public QFunding(Path<? extends Funding> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFunding(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFunding(PathMetadata metadata, PathInits inits) {
        this(Funding.class, metadata, inits);
    }

    public QFunding(Class<? extends Funding> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.back.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

