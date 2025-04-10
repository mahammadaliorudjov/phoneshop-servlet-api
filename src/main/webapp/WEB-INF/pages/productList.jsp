<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="products" type="java.util.ArrayList" scope="request"/>
<jsp:useBean id="cart" type="com.es.phoneshop.model.cart.Cart" scope="request"/>
<jsp:useBean id="recentlyViewedProducts" type="java.util.Deque" scope="request"/>
<tags:master pageTitle="Product List">
  <p>
    Welcome to Expert-Soft training!
  </p>
  <p>${cart.cartItems}</p>
  <c:if test="${not empty param.message and empty error}">
    <div class="success" style="color: green">
      ${param.message}
    </div>
  </c:if>
  <form>
    <input name="query" value="${param.query}">
    <button>Search</button>
  </form>
  <table>
    <thead>
      <tr>
        <td>Image</td>
        <td>
          Description
          <tags:sortLink sort="description" order="asc"/>
          <tags:sortLink sort="description" order="desc"/>
        </td>
        <td class="quantity">Quantity</td>
        <td class="price">
          Price
          <tags:sortLink sort="price" order="asc"/>
          <tags:sortLink sort="price" order="desc"/>
        </td>
      </tr>
    </thead>
    <c:forEach var="product" items="${products}">
      <form method="post" action="${pageContext.servletContext.contextPath}/products">
        <tr>
          <td>
            <img class="product-tile" src="${product.imageUrl}">
          </td>
          <td>
            <a href="${pageContext.servletContext.contextPath}/products/${product.id}">
              ${product.description}
            </a>
          </td>
          <td class="quantity">
            <c:set var="quantityString" value="${quantity}"/>
            <input name="quantity" value="${not empty error and errorProductId eq product.id ? quantityString : 1}" class="quantity"/>
            <input type="hidden" name="productId" value="${product.id}"/>
            <c:if test="${not empty error and errorProductId eq product.id}">
              <div class="error" style="color: red">
                ${error}
              </div>
            </c:if>
          </td>
          <td class="price">
            <a href="${pageContext.servletContext.contextPath}/products/price-history/${product.id}">
              <fmt:formatNumber value="${product.price}" type="currency" currencySymbol="${product.currency.symbol}"/>
            </a>
          </td>
          <td>
            <button>
              Add to cart
            </button>
          </td>
        </tr>
      </form>
    </c:forEach>
  </table>
    <c:if test="${not empty recentlyViewedProducts}">
      <h2>Recently viewed products</h2>
      <c:forEach var="product" items="${recentlyViewedProducts}">
        <div>
          <p>
            <img src="${product.imageUrl}">
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
    </c:if>
</tags:master>
