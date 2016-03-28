package jmri.profile;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;

/**
 * An empty JMRI application profile. Profiles allow a JMRI application to load
 * completely separate set of preferences at each launch without relying on host
 * OS-specific tricks to ensure this happens.
 * <p>
 * A NullProfile allows an application using JMRI as a library to set the active
 * JMRI profile to an identity set by that application, if the use of a standard
 * JMRI profile is not acceptable.
 *
 * @author rhwood Copyright (C) 2014
 * @see jmri.profile.ProfileManager#setActiveProfile(jmri.profile.Profile)
 */
public class NullProfile extends Profile {

//* This class seems to reimplement the parent
//* Profile class, _except_ for setting the local "path"
//* member variable in the constructor. Shouldn't it just
//* defer everything to the parent?

    private String name;
    private String id;
    private File path;

    /**
     * Create a NullProfile object given just a path to it. The Profile must
     * exist in storage on the computer.
     *
     * @param path The Profile's directory
     * @throws IOException
     */
    public NullProfile(File path) throws IOException {
        super(path, false);
    }

    /**
     * Create a Profile object and a profile in storage. A Profile cannot exist
     * in storage on the computer at the path given. Since this is a new
     * profile, the id must match the last element in the path.
     * <p>
     * This is the only time the id can be set on a Profile, as the id becomes a
     * read-only property of the Profile. The {@link ProfileManager} will only
     * load a single profile with a given id.
     *
     * @param name
     * @param id   If null, {@link jmri.profile.ProfileManager#createUniqueId()}
     *             will be used to generate the id.
     * @param path
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public NullProfile(String name, String id, File path) throws IOException, IllegalArgumentException {
        super(path, false);
        this.name = name;
        if (null != id) {
            this.id = id;
        } else {
            this.id = ProfileManager.createUniqueId();
        }
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the id
     */
    @Override
    public @Nonnull String getId() {
        return id;
    }

    /**
     * @return the path
     */
    @Override
    public File getPath() {
        return path;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NullProfile other = (NullProfile) obj;
        return !((this.id == null) ? (other.id != null) : !this.id.equals(other.id));
    }

    /**
     * Test if the profile is complete.
     *
     * @return always true for a NullProfile.
     */
    @Override
    public boolean isComplete() {
        return true;
    }

    /**
     * Return the uniqueness portion of the Profile Id.
     *
     * @return The complete Id of a NullProfile.
     */
    @Override
    public String getUniqueId() {
        return this.id; // NOI18N
    }
}
