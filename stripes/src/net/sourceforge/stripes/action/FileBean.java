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
package net.sourceforge.stripes.action;

import java.io.*;

/**
 * <p>Represents a file that was submitted as part of an HTTP POST request.  Provides methods for
 * examining information about the file, and the retreiving the contents of the file. When a file
 * is uploaded by a user it is stored as a temporary file on the file system, which is wrapped by an
 * instance of this class. This is necessary because browsers may send file upload segments before
 * sending any other form parameters needed to identify what to do with the uploaded files!</p>
 *
 * <p>The application developer is responsible for removing this temporary file once they have
 * processed it.  This can be accomplished in one of two ways.  Firstly a call to save(File) will
 * effect a save by <em>moving</em> the temporary file to the desired location.  In this case there
 * is no need to call delete(), although doing so will not delete the saved file. The second way is
 * to simply call delete().  This is more applicable when consuming the file as an InputStream. An
 * exmaple code fragment for reading a text based file might look like this:</p>
 *
 * <pre>
 * FileBean bean = getUserIcon();
 * BufferedReader reader = new BufferedReader( new InputStreamReader(bean.getInputStream()) );
 * String line = null
 *
 * while ( (line = reader.readLine()) != null) {
 *     // do something with line
 * }
 *
 * bean.delete();
 * </pre>
 *
 * @author Tim Fennell
 */
public class FileBean {
    private String contentType;
    private String fileName;
    private File file;
    private boolean saved;


    /**
     * Constructs a FileBean pointing to an on-disk representation of the file uploaded by the user.
     *
     * @param file the File object on the server which holds the uploaded contents of the file
     * @param contentType the content type of the file declared by the browser during uplaod
     * @param originalName the name of the file as declared by the user&apos;s browser
     */
    public FileBean(File file, String contentType, String originalName) {
        this.file = file;
        this.contentType = contentType;
        this.fileName = originalName;
    }

    /**
     * Returns the name of the file that the user selected and uplaoded (this is not necessarily
     * the name that the underlying file is now stored on the server using).
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the content type of the file that the user selected and uplaoded.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the size of the file that was uploaded.
     */
    public long getSize() {
        return this.file.length();
    }

    /**
     * Gets an input stream to read from the file uploaded
     */
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this.file);
    }

    /**
     * Saves the uploaded file to the location on disk represented by File.  First attemps a
     * simple rename of the underlying file that was created during upload as this is the
     * most efficient route. If the rename fails an attempt is made to copy the file bit
     * by bit to the new File and then the temporary file is removed.
     *
     * @param toFile a File object representing a location
     * @throws IOException if the save will fail for a reason that we can detect up front, for
     *         example, missing files, permissions etc. or we try to save get a failure.
     */
    public void save(File toFile) throws IOException {
        // Since File.renameTo doesn't tell you anything about why it failed, we test
        // for some common reasons for failure ahead of time and give a bit more info
        if (!this.file.exists()) {
            throw new IOException
                ("Some time between uploading and saving we lost the file "
                    + this.file.getAbsolutePath() + " - where did it go?.");
        }

        if (!this.file.canWrite()) {
            throw new IOException
                ("Some time between uploading and saving we lost the ability to write to the file "
                    + this.file.getAbsolutePath() + " - writability is required to move the file.");
        }

        File parent = toFile.getAbsoluteFile().getParentFile();
        if (toFile.exists() && !toFile.canWrite()) {
            throw new IOException("Cannot overwrite existing file at "+ toFile.getAbsolutePath());
        }
        else if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Parent directory of specified file does not exist and cannot " +
                " be created. File location supplied: " + toFile.getAbsolutePath());
        }
        else if (!toFile.exists() && !parent.canWrite()) {
            throw new IOException("Cannot create new file at location: " + toFile.getAbsolutePath());
        }

        this.saved = this.file.renameTo(toFile);

        // If the rename didn't work, try copying the darn thing bit by bit
        if (this.saved == false) {
            saveViaCopy(toFile);
        }
    }

    /**
     * Attempts to save the uploaded file to the specified file by performing a stream
     * based copy. This is only used when a rename cannot be executed, e.g. because the
     * target file is on a different file system than the temporary file.
     *
     * @param toFile the file to save to
     */
    protected void saveViaCopy(File toFile) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(toFile));
        BufferedInputStream   in = new BufferedInputStream(new FileInputStream(this.file));

        int b;
        while ((b = in.read()) != -1) {
            out.write(b);
        }

        in.close();
        out.close();

        this.file.delete();
        this.saved = true;
    }

    /**
     * Deletes the temporary file associated with this file upload if one still exists.  If save()
     * has already been called then there is no temporary file any more, and this is a no-op.
     *
     * @throws IOException if the delete will fail for a reason we can detect up front, or if
     *         we try to delete and get a failure
     */
    public void delete() throws IOException {
        if (!this.saved) {
            // Since File.delete doesn't tell you anything about why it failed, we test
            // for some common reasons for failure ahead of time and give a bit more info
            if (!this.file.exists()) {
                throw new IOException
                    ("Some time between uploading and saving we lost the file "
                        + this.file.getAbsolutePath() + " - where did it go?.");
            }

            if (!this.file.canWrite()) {
                throw new IOException
                    ("Some time between uploading and saving we lost the ability to write to the file "
                        + this.file.getAbsolutePath() + " - writability is required to delete the file.");
            }
            this.file.delete();
        }
    }

    /**
     * Returns the name of the file and the content type in a String format.
     */
    public String toString() {
        return "FileBean{" +
            "contentType='" + contentType + "'" +
            ", fileName='" + fileName + "'" +
            "}";
    }
}
