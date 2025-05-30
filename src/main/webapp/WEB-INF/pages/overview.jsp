<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="order" type="com.es.phoneshop.model.order.Order" scope="request"/>

<tags:master pageTitle="Overview">
  <table>
    <thead>
      <tr>
        <td>Image</td>
        <td>Description</td>
        <td class="quantity">Quantity</td>
        <td class="price">Price</td>
      </tr>
    </thead>
    <c:forEach var="item" items="${order.cartItems}" varStatus="status">
      <tr>
        <td>
          <img class="product-tile" src="${item.product.imageUrl}">
        </td>
        <td>
          <a href="${pageContext.servletContext.contextPath}/products/${item.product.id}">
            ${item.product.description}
          </a>
        </td>
        <td class="quantity">
          <fmt:formatNumber value="${item.quantity}" var="quantity"/>
            ${quantity}
        </td>
        <td class="price">
          <a href="${pageContext.servletContext.contextPath}/products/price-history/${item.product.id}">
            <fmt:formatNumber value="${item.product.price}" type="currency" currencySymbol="${item.product.currency.symbol}"/>
          </a>
        </td>
      </tr>
    </c:forEach>
    <tr>
      <td></td>
      <td></td>
      <td>Subtotal:</td>
      <td class="price">
        <p>
          <fmt:formatNumber value="${order.subtotal}" type="currency" currencySymbol="${order.cartItems[0].product.currency.symbol}"/>
        </p>
      </td>
    </tr>
    <tr>
      <td></td>
      <td></td>
      <td>Delivery cost:</td>
      <td class="price">
        <p>
          <fmt:formatNumber value="${order.deliveryCost}" type="currency" currencySymbol="${order.cartItems[0].product.currency.symbol}"/>
        </p>
      </td>
    </tr>
    <tr>
      <td></td>
      <td></td>
      <td>Total cost:</td>
      <td>
        <fmt:formatNumber value="${order.totalCost}" type="currency" currencySymbol="${order.cartItems[0].product.currency.symbol}"/>
      </td>
    </tr>
  </table>
  <h2>Your details</h2>
  <table>
    <tags:overviewRow name="firstName" label="First Name" order="${order}"></tags:overviewRow>
    <tags:overviewRow name="lastName" label="Last Name" order="${order}"></tags:overviewRow>
    <tags:overviewRow name="phone" label="Phone" order="${order}"></tags:overviewRow>
    <tags:overviewRow name="deliveryAddress" label="Delivery Address" order="${order}"></tags:overviewRow>
    <tags:overviewRow name="deliveryDate" label="Delivery Date" order="${order}"></tags:overviewRow>
    <tags:overviewRow name="paymentMethod" label="Payment Method" order="${order}"></tags:overviewRow>
  </table>
</tags:master>
