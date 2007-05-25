/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.UrlBuilder;
import net.sourceforge.stripes.controller.StripesConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * Abstract support class for generating links.  Used by both the LinkTag (which generates
 * regular {@literal <a href=""/>} style links) and the UrlTag which is a rough similie
 * of the JSTL url tag.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public abstract class LinkTagSupport extends HtmlTagSupport implements ParameterizableTag {
    private Map<String,Object> parameters = new HashMap<String,Object>();
    private String event;
    private Object beanclass;
    private String url;
    private boolean addSourcePage = false;

    /**
     * Gets the URL that is supplied by the user/developer on the page. This is the basis
     * for constructing the eventual URL that the tag generates.
     *
     * @return the URL that was supplied by the user
     */
    public String getUrl() { return url; }

    /**
     * Sets the URL that is supplied by the user/developer on the page. This is the basis
     * for constructing the eventual URL that the tag generates.
     *
     * @param url the URL supplied on the page
     */
    public void setUrl(String url) { this.url = url; }

    /**
     * Used by stripes:param tags (and possibly other tags at some distant point in
     * the future) to add a parameter to the parent link tag.
     *
     * @param name the name of the parameter(s) to add
     * @param valueOrValues
     */
    public void addParameter(String name, Object valueOrValues) {
        this.parameters.put(name, valueOrValues);
    }

    /** Retrieves the parameter values set on the tag. */
    public Map<String,Object> getParameters() {
        return this.parameters;
    }

    /**
     * Clears all existing parameters. Subclasses should be careful to call this in
     * doEndTag() to ensure that paremeter state is cleared between pooled used of the tag.
     */
    public void clearParameters() {
        this.parameters.clear();
    }

    /** Sets the (optional) event name that the link will trigger. */
    public void setEvent(String event) { this.event = event; }

    /** Gets the (optional) event name that the link will trigger. */
    public String getEvent() { return event; }

    /**
     * Sets the bean class (String FQN or Class) to generate a link for. Provides an
     * alternative to using href for targetting ActionBeans.
     *
     * @param beanclass the name of an ActionBean class, or Class object
     */
    public void setBeanclass(Object beanclass) { this.beanclass = beanclass; }

    /**
     * Gets the bean class (String FQN or Class) to generate a link for. Provides an
     * alternative to using href for targetting ActionBeans.
     *
     * @return the name of an ActionBean class, or Class object
     */
    public Object getBeanclass() { return beanclass; }

    /**
     * Get the flag that indicates if the _sourcePage parameter should be
     * appended to the URL.
     * 
     * @return true if _sourcePage is to be appended to the URL; false otherwise
     */
    public boolean isAddSourcePage() { return addSourcePage; }

    /**
     * Set the flag that indicates if the _sourcePage parameter should be
     * appended to the URL.
     */
    public void setAddSourcePage(boolean addSourcePage) { this.addSourcePage = addSourcePage; }

    /**
     * Returns the base URL that should be used for building the link. This is derived from
     * the 'beanclass' attribute if it is set, else from the 'url' attribute.
     *
     * @return the preferred base URL for the link
     * @throws StripesJspException if a beanclass attribute was specified, but does not identify
     *         an existing ActionBean
     */
    protected String getPreferredBaseUrl() throws StripesJspException {
        // If the beanclass attribute was supplied we'll prefer that to an href
        if (this.beanclass != null) {
            String beanHref = getActionBeanUrl(beanclass);
            if (beanHref == null) {
                throw new StripesJspException("The value supplied for the 'beanclass' attribute "
                        + "does not represent a valid ActionBean. The value supplied was '" +
                        this.beanclass + "'. If you're prototyping, or your bean isn't ready yet " +
                        "and you want this exception to go away, just use 'href' for now instead.");
            }
            else {
                return beanHref;
            }
        }
        else {
            return getUrl();
        }
    }

    /**
     * Builds the URL based on the information currently stored in the tag. Ensures that all
     * parameters are appended into the URL, along with event name if necessary and the source
     * page information.
     *
     * @return the fully constructed URL
     * @throws StripesJspException if the base URL cannot be determined
     */
    protected String buildUrl() throws StripesJspException {
        HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
        HttpServletResponse response = (HttpServletResponse) getPageContext().getResponse();

        String base = getPreferredBaseUrl();
        String contextPath = request.getContextPath();

        // Append the context path, but only if the user didn't already
        if (base.startsWith("/") && !"/".equals(contextPath)
                && !base.contains(contextPath + "/")) {
            base = contextPath + base;
        }

        // Add all the parameters and reset the href attribute; pass to false here because
        // the HtmlTagSupport will HtmlEncode the ampersands for us
        UrlBuilder builder = new UrlBuilder(base, false);
        if (this.event != null) {
            builder.addParameter(this.event);
        }
        if (addSourcePage) {
            builder.addParameter(StripesConstants.URL_KEY_SOURCE_PAGE, request.getServletPath());
        }
        builder.addParameters(this.parameters);

        return response.encodeURL(builder.toString());
    }
}
