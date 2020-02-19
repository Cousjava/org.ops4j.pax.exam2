/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.ops4j.pax.exam.Option;

/**
 * Default implementation of (@link CompositeOption}.
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 08, 2008
 */
public class DefaultCompositeOption implements ModifiableCompositeOption {

    /**
     * Composite options (cannot be null).
     */
    private final List<Option> options = new ArrayList<>();

    /**
     * Constructor.
     * 
     * @param options
     *            composite options (can be null or no option specified)
     */
    public DefaultCompositeOption(final Option... options) {
        add(options);
    }

    /**
     * Constructor.
     */
    public DefaultCompositeOption() {
    }

    private List<Option> expand(final Collection<Option> options) {
        final List<Option> expanded = new ArrayList<>();
        if (options != null) {
            for (final Option option : options) {
                if (option != null) {
                    if (option instanceof CompositeOption) {
                        expanded.addAll(Arrays.asList(((CompositeOption) option).getOptions()));
                    } else {
                        expanded.add(option);
                    }
                }
            }
        }
        return expanded;
    }

    private List<Option> expand(final Option... options) {
        return expand(Arrays.asList(options));
    }

    public Option[] getOptions() {
        return options.toArray(new Option[0]);
    }

    /**
     * Adds options.
     * 
     * @param _options
     *            composite options to be added (can be null or no options specified)
     * 
     * @return itself, for fluent api usage
     */
    public DefaultCompositeOption add(final Option... _options) {
        if (_options != null) {
            final List<Option> expanded = expand(_options);
            this.options.addAll(expanded);
        }
        return this;
    }

    /**
     * Remove options.
     *
     * @param options options to be removed from composite
     * @return itself, for fluent api usage
     */
    @Override
    public DefaultCompositeOption remove(Collection<Option> options) {
        final List<Option> expanded = expand(options);
        this.options.removeAll(expanded);
        return this;
    }

    /**
     * Remove options.
     *
     * @param options options to be removed from composite
     * @return itself, for fluent api usage
     */
    @Override
    public DefaultCompositeOption remove(Option... options) {
        return remove(Arrays.asList(options));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DefaultCompositeOption");
        sb.append("{options=").append(options);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        return result;
    }

    // CHECKSTYLE:OFF : generated code
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultCompositeOption other = (DefaultCompositeOption) obj;
        if (options == null) {
            if (other.options != null)
                return false;
        }
        else if (!options.equals(other.options))
            return false;
        return true;
    }
    // CHECKSTYLE:ON
}
