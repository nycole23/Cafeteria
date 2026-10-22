
package senai499.com.br.expressocertorio.repository;

import senai499.com.br.expressocertorio.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrueOrderByCategoriaAscNomeAsc();
}
