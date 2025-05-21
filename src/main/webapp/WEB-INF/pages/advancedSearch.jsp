<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="recentlyViewedProducts" type="java.util.Deque" scope="request"/>
<jsp:useBean id="searchMethods" scope="request" type="java.util.List"/>

<tags:master pageTitle="Product List">
  <p>
    Advanced search
  </p>
  <form>
    <p>
      Description
      <input name="query" value="${param.query}">
      <select name="searchMethod">
        <c:forEach var="searchMethod" items="${searchMethods}">
          <option ${searchMethod eq param['searchMethod'] ? 'selected="selected"' : ''} value="${searchMethod}">
              ${searchMethod.searchType}
          </option>
        </c:forEach>
      </select>
    </p>
    <tags:enterPrice name="minPrice" label="Min price" errors="${errors}"/>
    <tags:enterPrice name="maxPrice" label="Max price" errors="${errors}"/>
    <p></p>
    <button>Search</button>
  </form>
  <c:if test="${not empty products}">
  <table>
    <thead>
      <tr>
        <td>Image</td>
        <td>
          Description
        </td>
        <td class="price">
          Price
        </td>
      </tr>
    </thead>
    <c:forEach var="product" items="${products}">
      <tr>
        <td>
          <img class="product-tile" src="${product.imageUrl}">
        </td>
        <td>
          <a href="${pageContext.servletContext.contextPath}/products/${product.id}">
            ${product.description}
          </a>
        </td>
        <td class="price">
          <a href="${pageContext.servletContext.contextPath}/products/price-history/${product.id}">
            <fmt:formatNumber value="${product.price}" type="currency" currencySymbol="${product.currency.symbol}"/>
          </a>
        </td>
      </tr>
    </c:forEach>
  </table>
  </c:if>
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