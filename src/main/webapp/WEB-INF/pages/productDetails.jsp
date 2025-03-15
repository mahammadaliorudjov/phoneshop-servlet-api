<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="product" type="com.es.phoneshop.model.Product" scope="request"/>
<tags:master pageTitle="Product List">
    <p>
        ${product.description}
    </p>
    <table>
        <tr>
            <td>Image</td>
            <td>
                <img
                class="product-tile"
                 src="${product.imageUrl}"
                 style="width: 300px; height: auto; max-width: 100%;">
            </td>
        </tr>
        <tr>
            <td>Code</td>
            <td style="text-align: right;">${product.code}</td>
        </tr>
        <tr>
            <td>Stock</td>
            <td style="text-align: right;">${product.stock}</td>
        </tr>
        <tr>
            <td>Price</td>
            <td style="text-align: right;" class="price">
                <fmt:formatNumber value="${product.price}" type="currency" currencySymbol="${product.currency.symbol}"/>
            </td>
        </tr>
    </table>
</tags:master>
