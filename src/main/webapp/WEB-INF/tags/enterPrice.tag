<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="name" required="true" type="java.lang.String"%>
<%@ attribute name="label" required="true" type="java.lang.String"%>
<%@ attribute name="errors" required="true" type="java.util.HashMap"%>

<p>
    ${label}
    <input name="${name}" value="${param[name]}">
    <c:set var="error" value="${errors[name]}"/>
    <c:if test="${not empty error}">
        <div class="error">
                ${error}
        </div>
    </c:if>
</p>
