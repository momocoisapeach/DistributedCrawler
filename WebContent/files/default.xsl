<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
  <html>
  <body>
  <h2>Matched Documents</h2>
  <table border="4">
    <tr bgcolor="#9acd32">
      <th>Time</th>
      <th>Url</th>
      <th>Document</th>
    </tr>
    <xsl:for-each select="documentcollection/document">
    <tr>
      <td><xsl:value-of select="@crawled"/></td>
      <td><xsl:value-of select="@location"/></td>
      <td><xsl:value-of select="."/></td>
    </tr>
    </xsl:for-each>
  </table>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>