/*
 * Copyright (c) 2024 Roberto Leibman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ai

import io.qdrant.client.grpc.Collections
import io.qdrant.client.grpc.Points.Filter
import io.qdrant.client.{QdrantClient as JavaQdrantClient, QdrantGrpcClient}
import zio.*

object QdrantClient {

  def apply(
    host:                      String,
    rpcPort:                   Int,
    useTransportLayerSecurity: Boolean = false
  ): Task[QdrantClient] = {
    ZIO.attempt(
      new QdrantClient(JavaQdrantClient(QdrantGrpcClient.newBuilder(host, rpcPort, useTransportLayerSecurity).build()))
    )
  }

  def apply(
    grpc: QdrantGrpcClient
  ): QdrantClient =
    new QdrantClient(
      JavaQdrantClient(grpc)
    )

}

case class QdrantClient(jclient: JavaQdrantClient) {

  def collectionExists(collectionName: String): Task[Boolean] = {
    ZIO
      .fromFutureJava {
        jclient.collectionExistsAsync(collectionName)
      }.map(Boolean.unbox)
  }

  def createCollection(
    collectionName: String,
    distance:       Collections.Distance = Collections.Distance.Cosine,
    dimension:      Int = 384
  ): Task[Collections.CollectionOperationResponse] = {
    ZIO.fromFutureJava {
      jclient.createCollectionAsync(
        collectionName,
        Collections.VectorParams.newBuilder().setDistance(distance).setSize(dimension).build()
      )
    }
  }

  def count(
    collectionName: String,
    filter:         Option[Filter] = None
  ): Task[Long] = {
    import scala.language.unsafeNulls
    ZIO
      .fromFutureJava {
        jclient.countAsync(collectionName, filter.orNull, true)
      }.map(Long.unbox)
  }

}
