@import kh.edu.istad.platform.web.Page
@param Page page
@param kh.edu.istad.platform.iam.application.dto.AuthUserResult user
@template.layout(page=page,title=page.t("profile.edit"),body=@`
    <form method="post" action="/profile/edit">
        <input type="hidden" name="${page.csrfName()}" value="${page.csrfToken()}">
        <fieldset>
            <legend>
                ${page.t("profile.section.identity")}
            </legend>
            <div class="field-grid">
                <label>
                    ${page.t("email")}
                    <input name="email" type="email" value="${page.values().containsKey("email") ? page.v("email") : (user.email() == null ? "" : user.email())}">
                </label>
                <label>
                    ${page.t("givenName")}
                    <input name="givenName" type="text" value="${page.values().containsKey("givenName") ? page.v("givenName") : (user.givenName() == null ? "" : user.givenName())}">
                </label>
                <label>
                    ${page.t("familyName")}
                    <input name="familyName" type="text" value="${page.values().containsKey("familyName") ? page.v("familyName") : (user.familyName() == null ? "" : user.familyName())}">
                </label>
                <label>
                    ${page.t("middleName")}
                    <input name="middleName" type="text" value="${page.values().containsKey("middleName") ? page.v("middleName") : (user.middleName() == null ? "" : user.middleName())}">
                </label>
                <label>
                    ${page.t("nickname")}
                    <input name="nickname" type="text" value="${page.values().containsKey("nickname") ? page.v("nickname") : (user.nickname() == null ? "" : user.nickname())}">
                </label>
            </div>
        </fieldset>
        <fieldset>
            <legend>
                ${page.t("profile.section.personal")}
            </legend>
            <div class="field-grid">
                <label>
                    ${page.t("gender")}
                    <input name="gender" type="text" value="${page.values().containsKey("gender") ? page.v("gender") : (user.gender() == null ? "" : user.gender())}">
                </label>
                <label>
                    ${page.t("birthdate")}
                    <input type="date" name="birthdate" value="${page.values().containsKey("birthdate") ? page.v("birthdate") : java.util.Objects.toString(user.birthdate(), "")}">
                </label>
                <label>
                    ${page.t("locale")}
                    <input name="locale" type="text" value="${page.values().containsKey("locale") ? page.v("locale") : (user.locale() == null ? "" : user.locale())}">
                </label>
                <label>
                    ${page.t("zoneinfo")}
                    <input name="zoneinfo" type="text" value="${page.values().containsKey("zoneinfo") ? page.v("zoneinfo") : (user.zoneinfo() == null ? "" : user.zoneinfo())}">
                </label>
                <label>
                    ${page.t("profileUrl")}
                    <input name="profileUrl" type="text" value="${page.values().containsKey("profileUrl") ? page.v("profileUrl") : (user.profileUrl() == null ? "" : user.profileUrl())}">
                </label>
                <label>
                    ${page.t("pictureUrl")}
                    <input name="pictureUrl" type="text" value="${page.values().containsKey("pictureUrl") ? page.v("pictureUrl") : (user.pictureUrl() == null ? "" : user.pictureUrl())}">
                </label>
                <label>
                    ${page.t("websiteUrl")}
                    <input name="websiteUrl" type="text" value="${page.values().containsKey("websiteUrl") ? page.v("websiteUrl") : (user.websiteUrl() == null ? "" : user.websiteUrl())}">
                </label>
            </div>
        </fieldset>
        <fieldset>
            <legend>
                ${page.t("profile.section.contact")}
            </legend>
            <div class="field-grid">
                <label>
                    ${page.t("phoneNumber")}
                    <input name="phoneNumber" type="text" value="${page.values().containsKey("phoneNumber") ? page.v("phoneNumber") : (user.phoneNumber() == null ? "" : user.phoneNumber())}">
                </label>
            </div>
        </fieldset>
        <fieldset>
            <legend>
                ${page.t("profile.section.address")}
            </legend>
            <div class="field-grid">
                <label>
                    ${page.t("addressStreetAddress")}
                    <input name="addressStreetAddress" type="text" value="${page.values().containsKey("addressStreetAddress") ? page.v("addressStreetAddress") : (user.addressStreetAddress() == null ? "" : user.addressStreetAddress())}">
                </label>
                <label>
                    ${page.t("addressLocality")}
                    <input name="addressLocality" type="text" value="${page.values().containsKey("addressLocality") ? page.v("addressLocality") : (user.addressLocality() == null ? "" : user.addressLocality())}">
                </label>
                <label>
                    ${page.t("addressRegion")}
                    <input name="addressRegion" type="text" value="${page.values().containsKey("addressRegion") ? page.v("addressRegion") : (user.addressRegion() == null ? "" : user.addressRegion())}">
                </label>
                <label>
                    ${page.t("addressPostalCode")}
                    <input name="addressPostalCode" type="text" value="${page.values().containsKey("addressPostalCode") ? page.v("addressPostalCode") : (user.addressPostalCode() == null ? "" : user.addressPostalCode())}">
                </label>
                <label>
                    ${page.t("addressCountry")}
                    <input name="addressCountry" type="text" value="${page.values().containsKey("addressCountry") ? page.v("addressCountry") : (user.addressCountry() == null ? "" : user.addressCountry())}">
                </label>
            </div>
        </fieldset>
        <button>
            ${page.t("common.save")}
        </button>
    </form>
`)
