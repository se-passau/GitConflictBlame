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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("File;Revision;Author;Lines");
        for (String conflictFile : args) {
            HashMap<String, HashMap<String, Integer>> blame
                    = GitConflictBlame.blame(new File(conflictFile));

            for (Map.Entry<String, HashMap<String, Integer>> entry : blame.entrySet()) {
                String revision = entry.getKey();
                HashMap<String, Integer> authors = entry.getValue();

                authors.keySet().forEach(author ->
                        System.out.println(String.format("%s;%s;%s;%d",
                                conflictFile, revision, author, authors.get(author))));
            }
        }
    }
}
