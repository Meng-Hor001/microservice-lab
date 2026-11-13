@import kh.edu.istad.platform.web.Page

@param Page page
@param kh.edu.istad.platform.iam.application.dto.AuthUserResult user
@param kh.edu.istad.platform.iam.application.dto.UserAuthoritiesResult authorities
@template.layout(page=page,title=page.t("profile.title"),body=@`
    <dl>
        <dt>
            ${page.t("uuid")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.uuid(), "")}
        </dd>
        <dt>
            ${page.t("username")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.username(), "")}
        </dd>
        <dt>
            ${page.t("email")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.email(), "")}
        </dd>
        <dt>
            ${page.t("emailVerified")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.emailVerified(), "")}
        </dd>
        <dt>
            ${page.t("givenName")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.givenName(), "")}
        </dd>
        <dt>
            ${page.t("familyName")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.familyName(), "")}
        </dd>
        <dt>
            ${page.t("middleName")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.middleName(), "")}
        </dd>
        <dt>
            ${page.t("nickname")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.nickname(), "")}
        </dd>
        <dt>
            ${page.t("gender")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.gender(), "")}
        </dd>
        <dt>
            ${page.t("birthdate")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.birthdate(), "")}
        </dd>
        <dt>
            ${page.t("profileUrl")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.profileUrl(), "")}
        </dd>
        <dt>
            ${page.t("pictureUrl")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.pictureUrl(), "")}
        </dd>
        <dt>
            ${page.t("websiteUrl")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.websiteUrl(), "")}
        </dd>
        <dt>
            ${page.t("zoneinfo")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.zoneinfo(), "")}
        </dd>
        <dt>
            ${page.t("locale")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.locale(), "")}
        </dd>
        <dt>
            ${page.t("phoneNumber")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.phoneNumber(), "")}
        </dd>
        <dt>
            ${page.t("phoneNumberVerified")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.phoneNumberVerified(), "")}
        </dd>
        <dt>
            ${page.t("addressStreetAddress")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.addressStreetAddress(), "")}
        </dd>
        <dt>
            ${page.t("addressLocality")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.addressLocality(), "")}
        </dd>
        <dt>
            ${page.t("addressRegion")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.addressRegion(), "")}
        </dd>
        <dt>
            ${page.t("addressPostalCode")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.addressPostalCode(), "")}
        </dd>
        <dt>
            ${page.t("addressCountry")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.addressCountry(), "")}
        </dd>
        <dt>
            ${page.t("updatedAt")}
        </dt>
        <dd>
            ${java.util.Objects.toString(user.updatedAt(), "")}
        </dd>
    </dl>
    <h2>
        ${page.t("roles")}
    </h2>
    <p>
        ${authorities.roles().stream().map(role -> role.name()).collect(java.util.stream.Collectors.joining(", "))}
    </p>
    <h2>
        ${page.t("permissions")}
    </h2>
    <p>
        ${String.join(", ", authorities.effectivePermissions())}
    </p>
    <a href="/profile/password">
        ${page.t("profile.password")}
    </a>
    <form method="post" action="/logout">
        <input type="hidden" name="${page.csrfName()}" value="${page.csrfToken()}">
        <button>
            ${page.t("common.logout")}
        </button>
    </form>
`)
