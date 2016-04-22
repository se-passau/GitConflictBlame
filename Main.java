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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Chunk-based");
        System.out.println("File;Revision;Author;NumLines;Lines");
        for (String conflictFile : args) {
            // show chunk-based stats
            HashMap<String, HashMap<String, List<List<Integer>>>> blameChunks
                    = GitConflictBlame.blameChunks(new File(conflictFile));

            for (Map.Entry<String, HashMap<String, List<List<Integer>>>> entry : blameChunks
                    .entrySet()) {
                String revision = entry.getKey();
                HashMap<String, List<List<Integer>>> authors = entry.getValue();

                authors.keySet().stream().forEach(author -> authors.get(author).stream().
                        forEach(chunk -> System.out.println(String.format("%s;%s;%s;%d;[%s]",
                                conflictFile,
                                revision,
                                author,
                                chunk.size(),
                                chunk.stream().map(String::valueOf)
                                        .collect(Collectors.joining(","))))));
            }
        }

        System.out.println();
        System.out.println("File-based");
        System.out.println("File;Revision;Author;NumLines;Lines");
        for (String conflictFile : args) {

            // show file-based stats
            HashMap<String, HashMap<String, List<Integer>>> blameFile
                    = GitConflictBlame.blameFile(new File(conflictFile));

            for (Map.Entry<String, HashMap<String, List<Integer>>> entry : blameFile.entrySet()) {
                String revision = entry.getKey();
                HashMap<String, List<Integer>> authors = entry.getValue();

                authors.keySet().forEach(author ->
                        System.out.println(String.format("%s;%s;%s;%d;[%s]",
                                conflictFile,
                                revision,
                                author,
                                authors.get(author).stream().count(),
                                authors.get(author).stream()
                                        .map(String::valueOf).collect(Collectors.joining(",")))));
            }
        }
    }
}
