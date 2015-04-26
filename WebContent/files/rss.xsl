<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:output method="html" indent="yes" 
     doctype-system='http://www.w3.org/TR/html4/strict.dtd'
     doctype-public='-//W3C//DTD HTML 4.01//EN' 
  />
  
  
  <xsl:template match="/">
  <html>
  <body>
  <h2>RSS Feed</h2>
    <xsl:for-each select="rsscollection/document">
      <p><xsl:apply-templates select="rss"/></p><br></br>
    </xsl:for-each>
  </body>
  </html>
</xsl:template>

  <xsl:template match="rss">
        <xsl:apply-templates select="channel" />
  </xsl:template>

  <xsl:template match="channel">
   <h3><xsl:element name="a">
    	<xsl:attribute name="href">
        	<xsl:value-of select="link"/>
    	</xsl:attribute>
    	<xsl:value-of select="title"/>
	</xsl:element></h3>
    <table cellpadding="2" cellspacing="0" border="0" width="75%">
      <xsl:apply-templates select="item" />
    </table>
  </xsl:template>

  <xsl:template match="item">
  <xsl:if test="(contains(title, 'war') or (contains(description, 'war')) or contains(title, 'peace') or (contains(description, 'peace')))">
    <!-- ... -->
    <h4><xsl:value-of select="title"></xsl:value-of></h4>
    <xsl:element name="a">
    	<xsl:attribute name="href">
        	<xsl:value-of select="link"/>
    	</xsl:attribute>
    	<xsl:value-of select="description"/>
	</xsl:element>
<!--     <tr>
      <td colspan="2" style="text-align:left;padding-top:10px;">
        <xsl:value-of select="description" disable-output-escaping="yes" />
      </td>
    </tr> -->
    <!-- ... -->
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>