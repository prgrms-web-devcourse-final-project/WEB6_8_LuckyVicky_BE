package com.back.domain.artist.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QArtistApplication is a Querydsl query type for ArtistApplication
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArtistApplication extends EntityPathBase<ArtistApplication> {

    private static final long serialVersionUID = 1901418421L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QArtistApplication artistApplication = new QArtistApplication("artistApplication");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final StringPath accountName = createString("accountName");

    public final StringPath artistName = createString("artistName");

    public final StringPath bankAccount = createString("bankAccount");

    public final StringPath bankName = createString("bankName");

    public final StringPath businessAddress = createString("businessAddress");

    public final StringPath businessAddressDetail = createString("businessAddressDetail");

    public final StringPath businessName = createString("businessName");

    public final StringPath businessNumber = createString("businessNumber");

    public final StringPath businessZipCode = createString("businessZipCode");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final ListPath<ArtistDocument, QArtistDocument> documents = this.<ArtistDocument, QArtistDocument>createList("documents", ArtistDocument.class, QArtistDocument.class, PathInits.DIRECT2);

    public final StringPath email = createString("email");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath mainProducts = createString("mainProducts");

    public final StringPath managerPhone = createString("managerPhone");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final StringPath ownerName = createString("ownerName");

    public final StringPath phone = createString("phone");

    public final StringPath rejectionReason = createString("rejectionReason");

    public final DateTimePath<java.time.LocalDateTime> reviewedAt = createDateTime("reviewedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> reviewedById = createNumber("reviewedById", Long.class);

    public final StringPath reviewedByName = createString("reviewedByName");

    public final StringPath snsAccount = createString("snsAccount");

    public final EnumPath<ApplicationStatus> status = createEnum("status", ApplicationStatus.class);

    public final StringPath telecomSalesNumber = createString("telecomSalesNumber");

    public final com.back.domain.user.entity.QUser user;

    public QArtistApplication(String variable) {
        this(ArtistApplication.class, forVariable(variable), INITS);
    }

    public QArtistApplication(Path<? extends ArtistApplication> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QArtistApplication(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QArtistApplication(PathMetadata metadata, PathInits inits) {
        this(ArtistApplication.class, metadata, inits);
    }

    public QArtistApplication(Class<? extends ArtistApplication> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.back.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

