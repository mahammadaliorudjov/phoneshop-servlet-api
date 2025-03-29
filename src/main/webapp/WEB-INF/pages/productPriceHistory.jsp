<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="priceHistory" type="java.util.ArrayList" scope="request"/>
<tags:master pageTitle="Price History">
  <p>
    Price History
  </p>
  <p var="priceHistory">
    ${priceHistory[0].description}
  </p>
  <table>
    <thead>
    <tr>
      <td>Date</td>
      <td>Price</td>
    </tr>
    </thead>
    <c:forEach var="priceHistory" items="${priceHistory}">
      <tr>
        <td>
          ${priceHistory.date}
        </td>
        <td class="price">
          <fmt:formatNumber value="${priceHistory.price}" type="currency"
                            currencySymbol="${priceHistory.currency.symbol}"/>
        </td>
      </tr>
    </c:forEach>
  </table>
</tags:master>
