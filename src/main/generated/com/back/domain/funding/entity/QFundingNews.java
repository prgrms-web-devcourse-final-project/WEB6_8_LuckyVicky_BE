package com.back.domain.funding.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFundingNews is a Querydsl query type for FundingNews
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFundingNews extends EntityPathBase<FundingNews> {

    private static final long serialVersionUID = -1044007546L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFundingNews fundingNews = new QFundingNews("fundingNews");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final com.back.domain.user.entity.QUser artist;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final QFunding funding;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath imageUrl = createString("imageUrl");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final StringPath title = createString("title");

    public QFundingNews(String variable) {
        this(FundingNews.class, forVariable(variable), INITS);
    }

    public QFundingNews(Path<? extends FundingNews> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFundingNews(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFundingNews(PathMetadata metadata, PathInits inits) {
        this(FundingNews.class, metadata, inits);
    }

    public QFundingNews(Class<? extends FundingNews> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.artist = inits.isInitialized("artist") ? new com.back.domain.user.entity.QUser(forProperty("artist")) : null;
        this.funding = inits.isInitialized("funding") ? new QFunding(forProperty("funding"), inits.get("funding")) : null;
    }

}

