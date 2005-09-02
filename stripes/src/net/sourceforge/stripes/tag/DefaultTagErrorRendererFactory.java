/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.config.Configuration;

/**
 * A basic implementation of the TagErrorRendererFactory interface that always
 * constructs and returns the {@link DefaultTagErrorRenderer}.
 *
 * @author Greg Hinkle
 */
public class DefaultTagErrorRendererFactory implements TagErrorRendererFactory {
    private Configuration configuration;

    /** Just stores the configuration passed in. */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
    }

    /**
     * Always returns an initialized instance of DefaultTagErrorRenderer.
     */
    public TagErrorRenderer getTagErrorRenderer(InputTagSupport tag) {
        TagErrorRenderer renderer = new DefaultTagErrorRenderer();
        renderer.init(tag);
        return renderer;
    }
}
