<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="cart" scope="request" type="com.es.phoneshop.model.cart.Cart"/>
<a href="${pageContext.servletContext.contextPath}/cart" style="color:black">Cart: ${cart.totalQuantity} items</a>
