package com.back.domain.auth.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserToken is a Querydsl query type for UserToken
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserToken extends EntityPathBase<UserToken> {

    private static final long serialVersionUID = -802299687L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserToken userToken = new QUserToken("userToken");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isActive = createBoolean("isActive");

    public final EnumPath<com.back.domain.user.entity.Role> loginRole = createEnum("loginRole", com.back.domain.user.entity.Role.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final StringPath refreshToken = createString("refreshToken");

    public final com.back.domain.user.entity.QUser user;

    public QUserToken(String variable) {
        this(UserToken.class, forVariable(variable), INITS);
    }

    public QUserToken(Path<? extends UserToken> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserToken(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserToken(PathMetadata metadata, PathInits inits) {
        this(UserToken.class, metadata, inits);
    }

    public QUserToken(Class<? extends UserToken> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.back.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

