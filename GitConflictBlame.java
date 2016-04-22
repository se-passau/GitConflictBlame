/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Olaf Lessenich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

class GitConflictBlame {

    private static final String CONFLICT_START = "<<<<<<<";
    private static final String CONFLICT_SEP = "=======";
    private static final String CONFLICT_END = ">>>>>>>";

    private static final String BLAME_CMD = "git blame -e";

    // we need this to disable the pager
    private static final String[] BLAME_ENV = {"GIT_PAGER=cat"};

    static HashMap<String, HashMap<String, Integer>> blame(File conflictFile) throws IOException {
        /*
         * Track location by using the following encoding for the values:
         * -1 = out of conflict
         *  0 = in variant1
         *  1 = in variant2
         */
        int location = -1;

        // no octopus merges supported for now ;)
        String[] revisions = new String[2];

        HashMap<String, HashMap<String, Integer>> result = new HashMap<>();
        HashMap<String, Integer> chunkAuthors = new HashMap<>();

        // run blame
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(BLAME_CMD + " " + conflictFile,
                BLAME_ENV, conflictFile.getParentFile());

        // parse output
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;

        while ((line = buf.readLine()) != null) {
            // hack line sby blame into useful output
            line = line.replaceAll("^.+?\\(<(.+?)>.+?\\)(.+?)", "$1:$2");
            String author = line.split(":")[0];
            String content = line.split(":", 2)[1].trim();

            // control flow seems a bit odd, I did this to collect the revision names before
            // pushing the authors into the hashmap

            if (content.startsWith(CONFLICT_START)) {
                location = 0;
                revisions[0] = content.split(" ", 2)[1];
                continue;
            } else if (content.startsWith(CONFLICT_SEP)) {
                location = 1;
            } else if (content.startsWith(CONFLICT_END)) {
                revisions[1] = content.split(" ", 2)[1];
                location = -1;
            } else if (location >= 0) {
                int contributions = chunkAuthors.containsKey(author)
                        ? chunkAuthors.get(author) + 1
                        : 1;
                chunkAuthors.put(author, contributions);
                continue;
            } else {
                continue;
            }

            // we are at separator or end of a conflict, processing authors found in chunk

            String revision = location == -1 ? revisions[1] : revisions[0];
            result.putIfAbsent(revision, new HashMap<>());
            HashMap<String, Integer> authors = result.get(revision);

            for (Map.Entry<String, Integer> entry : chunkAuthors.entrySet()) {
                String chunkAuthor = entry.getKey();
                int contribution = authors.containsKey(chunkAuthor)
                        ? authors.get(chunkAuthor) + entry.getValue()
                        : entry.getValue();
                authors.put(chunkAuthor, contribution);
            }

            chunkAuthors.clear();

        }

        buf.close();

        if (pr.exitValue() != 0) {
            throw new RuntimeException(String.format("Error on external call with exit code %d",
                    pr.exitValue()));
        }

        pr.getInputStream().close();
        pr.getErrorStream().close();
        pr.getOutputStream().close();

        return result;
    }
}
