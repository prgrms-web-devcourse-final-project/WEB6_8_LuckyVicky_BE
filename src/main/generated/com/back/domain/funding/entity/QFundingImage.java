package com.back.domain.funding.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFundingImage is a Querydsl query type for FundingImage
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFundingImage extends EntityPathBase<FundingImage> {

    private static final long serialVersionUID = 1991103752L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFundingImage fundingImage = new QFundingImage("fundingImage");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final EnumPath<com.back.global.s3.FileType> fileType = createEnum("fileType", com.back.global.s3.FileType.class);

    public final StringPath fileUrl = createString("fileUrl");

    public final QFunding funding;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final StringPath originalFilename = createString("originalFilename");

    public final StringPath s3Key = createString("s3Key");

    public QFundingImage(String variable) {
        this(FundingImage.class, forVariable(variable), INITS);
    }

    public QFundingImage(Path<? extends FundingImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFundingImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFundingImage(PathMetadata metadata, PathInits inits) {
        this(FundingImage.class, metadata, inits);
    }

    public QFundingImage(Class<? extends FundingImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.funding = inits.isInitialized("funding") ? new QFunding(forProperty("funding"), inits.get("funding")) : null;
    }

}

