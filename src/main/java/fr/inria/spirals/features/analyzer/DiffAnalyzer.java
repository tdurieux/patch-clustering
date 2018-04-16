package fr.inria.spirals.features.analyzer;

import fr.inria.spirals.entities.Change;
import fr.inria.spirals.entities.Changes;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.patch.Patch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by tdurieux
 */
public class DiffAnalyzer {

	private final Patch patch;
	private int nbFiles;

	public DiffAnalyzer(String diffPath) {
		this.patch = new Patch();
		try {
			patch.parse(new FileInputStream(diffPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Changes analyze() {
		Changes changes = new Changes();
		this.nbFiles = patch.getFiles().size();
		for (int i = 0; i < nbFiles; i++) {
			FileHeader fileHeader = patch.getFiles().get(i);
			List<? extends HunkHeader> hunks = fileHeader.getHunks();
			for (int j = 0; j < hunks.size(); j++) {
				HunkHeader hunkHeader = hunks.get(j);
				EditList edits = hunkHeader.toEditList();
				for (int k = 0; k < edits.size(); k++) {
					Edit edit = edits.get(k);
					Change oldChange = new Change(edit.getType().name(),
							fileHeader.getOldPath().trim(),
							edit.getBeginA() + 1, edit.getEndA(),
							edit.getLengthA());
					Change newChange = new Change(edit.getType().name(),
							fileHeader.getNewPath().trim(),
							edit.getBeginB() + 1, edit.getEndB(),
							edit.getLengthB());

					newChange.setAssociateChange(oldChange);
					oldChange.setAssociateChange(newChange);

					changes.addOldChange(oldChange);
					changes.addNewChange(newChange);
				}
			}
		}
		return changes;
	}

	public Map<String, List<String>> getOriginalFiles(String projectRoot) {
		Map<String, List<String>> output = new HashMap<>(patch.getFiles().size());
		for (int i = 0; i < patch.getFiles().size(); i++) {
			FileHeader fileHeader = patch.getFiles().get(i);
			String fileName = fileHeader.getOldPath().trim();
			if (!fileName.endsWith(".java")) {
				continue;
			}
			fileName = getFullPath(projectRoot, fileName);
			output.put(fileName, fileToLines(fileName));
		}
		return output;
	}



	public Map<String, List<String>> getPatchedFiles(String projectRoot) {
		Map<String, List<String>> output = getOriginalFiles(projectRoot);
		for (int i = 0; i < patch.getFiles().size(); i++) {
			FileHeader fileHeader = patch.getFiles().get(i);
			String fileName = getFullPath(projectRoot, fileHeader.getOldPath().trim());
			if (!output.containsKey(fileName)) {
				continue;
			}
			for (HunkHeader hh : fileHeader.getHunks()) {
				byte[] b = new byte[hh.getEndOffset() - hh.getStartOffset()];
				System.arraycopy(hh.getBuffer(), hh.getStartOffset(), b, 0, b.length);
				RawText hrt = new RawText(b);
				List<String> hunkLines = new ArrayList(hrt.size());


				for(int pos = 0; pos < hrt.size(); ++pos) {
					hunkLines.add(hrt.getString(pos));
				}

				int pos = 0;
				for(int j = 1; j < hunkLines.size(); ++j) {
					String hunkLine = hunkLines.get(j);
					switch(hunkLine.charAt(0)) {
					case ' ':
						++pos;
						break;
					case '+':
						int index = hh.getNewStartLine() - 1 + pos;
						output.get(fileName).add(index, hunkLine.substring(1));
						++pos;
						break;
					case '-':
						if (hh.getNewStartLine() == 0) {
							output.get(fileName).clear();
						} else {
							index = hh.getNewStartLine() - 1 + pos;
							if (pos == 0 && hh.getNewLineCount() == 0) {
								index++;
							}
							output.get(fileName).remove(index);
						}
					}
				}
			}
		}
		return output;
	}

	private String getFullPath(String projectRoot, String fileName) {
		if (!fileName.startsWith("/")) {
			fileName = "/" + fileName;
		}
		if (!fileName.contains(projectRoot)) {
			fileName = projectRoot + fileName;
		}
		return fileName;
	}

	// Helper method for get the file content
	private List<String> fileToLines(String filename) {
		List<String> lines = new LinkedList<String>();
		String line = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public int getNbFiles() {
		return nbFiles;
	}
}