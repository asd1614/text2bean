/*
 * Copyright 2019 asd1614
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

package io.github.asd1614.text.parse;

import io.github.asd1614.text.parse.support.spel.MappingEvaluationContext;

public interface TextProcessor {

    public static final String CURRENT_CONFIG = "currentConfig";

    public static final String CURRENT_LINE = "currentLine";

    public static final String CURRENT_INDEX = "currentIndex";

    public static final String CONFIG_QUEUE = "configQueue";

    public static final String FULL_TEXTLINES = "fullTextLines";

    public static final String FULL_TEXT = "fullText";

    public static final String PARSER_DATAPOLL = "dataPool";

    public boolean singleParse(MappingEvaluationContext context);

    public boolean listParse(MappingEvaluationContext context);

    public boolean blockParse(MappingEvaluationContext context);

    public boolean ignoreParse(MappingEvaluationContext context);
}
