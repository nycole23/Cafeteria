package senai499.com.br.expressocertorio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senai499.com.br.expressocertorio.model.ItemPedido;

public interface ItemPedidoRepository
        extends JpaRepository<ItemPedido, Long> {
}