<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="cart" type="com.es.phoneshop.model.cart.Cart" scope="request"/>
<tags:master pageTitle="Cart">
  <c:if test="${not empty param.message}">
    <div class="success" style="color: green">
      ${param.message}
    </div>
  </c:if>
  <c:if test="${not empty errors}">
    <div class="error" style="color: red">
      There were errors during cart update
    </div>
  </c:if>
  <form method="post" action="${pageContext.servletContext.contextPath}/cart">
    <table>
      <thead>
        <tr>
          <td>Image</td>
          <td>Description</td>
          <td class="price">Price</td>
          <td class="price">Quantity</td>
          <td></td>
        </tr>
      </thead>
      <c:forEach var="item" items="${cart.cartItems}" varStatus="status">
        <tr>
          <td>
            <img class="product-tile" src="${item.product.imageUrl}">
          </td>
          <td>
            <a href="${pageContext.servletContext.contextPath}/products/${item.product.id}">
              ${item.product.description}
            </a>
          </td>
          <td class="price">
            <a href="${pageContext.servletContext.contextPath}/products/price-history/${item.product.id}">
              <fmt:formatNumber value="${item.product.price}" type="currency" currencySymbol="${item.product.currency.symbol}"/>
            </a>
          </td>
          <td class="quantity">
            <fmt:formatNumber value="${item.quantity}" var="quantity"/>
            <c:set var="error" value="${errors[item.product.id]}"/>
            <input name="quantity" value="${not empty error ? quantities[item.product.id] : item.quantity}" class="quantity"/>
            <input type="hidden" name="productId" value="${item.product.id}"/>
            <c:if test="${not empty error}">
              <div class="error" style="color: red">
                ${errors[item.product.id]}
              </div>
            </c:if>
          </td>
          <td>
            <button form="deleteCartItem"
                    formaction="${pageContext.servletContext.contextPath}/cart/deleteCartItem/${item.product.id}">Delete
                    </button>
          </td>
        </tr>
      </c:forEach>
      <tr>
        <td colspan="2"></td>
        <td class="price">
          Total cost:
          <fmt:formatNumber value="${cart.totalCost}" type="currency" currencySymbol="${cart.cartItems[0].product.currency.symbol}"/>
        </td>
        <td class="quantity">
          Total Quantity:
          ${cart.totalQuantity}
        </td>
        <td></td>
      </tr>
    </table>
    <p>
      <button>Update</button>
    </p>
  </form>
  <c:choose>
    <c:when test="${empty errors}">
      <button onclick="window.location.href='${pageContext.servletContext.contextPath}/checkout'">
        Checkout
      </button>
    </c:when>
    <c:otherwise>
      <button disabled style="opacity: 0.6; cursor: not-allowed;">
        Checkout
      </button>
      <div class="error" style="color: red">
        Please correct the errors in your cart before checkout
      </div>
    </c:otherwise>
  </c:choose>
  <form id='deleteCartItem' method="post"></form>
</tags:master>
