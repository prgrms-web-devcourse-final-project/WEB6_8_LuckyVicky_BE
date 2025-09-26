package com.back.domain.artist.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QArtistDocument is a Querydsl query type for ArtistDocument
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArtistDocument extends EntityPathBase<ArtistDocument> {

    private static final long serialVersionUID = 1338646742L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QArtistDocument artistDocument = new QArtistDocument("artistDocument");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final QArtistApplication artistApplication;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final EnumPath<DocumentType> documentType = createEnum("documentType", DocumentType.class);

    public final StringPath fileName = createString("fileName");

    public final StringPath fileUrl = createString("fileUrl");

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final StringPath s3Key = createString("s3Key");

    public QArtistDocument(String variable) {
        this(ArtistDocument.class, forVariable(variable), INITS);
    }

    public QArtistDocument(Path<? extends ArtistDocument> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QArtistDocument(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QArtistDocument(PathMetadata metadata, PathInits inits) {
        this(ArtistDocument.class, metadata, inits);
    }

    public QArtistDocument(Class<? extends ArtistDocument> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.artistApplication = inits.isInitialized("artistApplication") ? new QArtistApplication(forProperty("artistApplication"), inits.get("artistApplication")) : null;
    }

}

