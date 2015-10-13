package ch.mohlerm.domain.psql;

import ch.mohlerm.domain.Client;

/**
 * Created by marcel on 9/29/15.
 */
public class PsqlClient implements Client {

    public PsqlClient(int id) {
        this.id = id;
    }
    private int id;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id1) {
        id = id1;
    }
}
