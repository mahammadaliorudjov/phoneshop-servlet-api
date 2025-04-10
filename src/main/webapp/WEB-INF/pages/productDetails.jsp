<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="product" type="com.es.phoneshop.model.product.Product" scope="request"/>
<jsp:useBean id="cart" type="com.es.phoneshop.model.cart.Cart" scope="request"/>
<jsp:useBean id="recentlyViewedProducts" type="java.util.Deque" scope="request"/>
<tags:master pageTitle="Product List">
  <h2>
    ${product.description}
  </h2>
  <c:if test="${not empty param.message and empty error}">
    <div class="success" style="color: green">
      ${param.message}
    </div>
  </c:if>
  <c:if test="${not empty error}">
    <div class="error" style="color: red">
      ${error}
    </div>
  </c:if>
  <p>${cart.cartItems}</p>
  <form method="post">
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
      <tr>
        <td>quantity</td>
        <td>
          <input name="quantity" value="${not empty quantity ? quantity : 1}" required>
        </td>
      </tr>
  </table>
  <button>Add to cart</button>
  </form>
<c:if test="${not empty recentlyViewedProducts}">
  <h2>Recently viewed products</h2>
  <div style="display: flex; flex-wrap: wrap; gap: 20px;">
    <c:forEach var="product" items="${recentlyViewedProducts}">
      <div style="padding: 10px;">
        <p>
          <img src="${product.imageUrl}" alt="${product.description}">
        </p>
        <p>
          <a href="${pageContext.servletContext.contextPath}/products/${product.id}">
            ${product.description}
          </a>
        </p>
        <p>
          <fmt:formatNumber value="${product.price}" type="currency" currencySymbol="${product.currency.symbol}"/>
        </p>
      </div>
    </c:forEach>
  </div>
</c:if>
</tags:master>
