package io.mehow.squashit

fun CredentialsQueries.upsert(credentials: Credentials, transact: Boolean = true) {
  fun doUpsert() {
    update(credentials.id, credentials.secret)
    if (changes().executeAsOne() == 0L) insertOrFail(credentials)
  }
  if (transact) transaction { doUpsert() } else doUpsert()
}
