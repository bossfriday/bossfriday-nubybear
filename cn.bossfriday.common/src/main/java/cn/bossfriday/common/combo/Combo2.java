package cn.bossfriday.common.combo;

import lombok.*;

/**
 * Combo2
 *
 * @author chenx
 */
@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Combo2<T1, T2> {

    private T1 v1;
    private T2 v2;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.v1 == null) ? 0 : this.v1.hashCode());
        result = prime * result + ((this.v2 == null) ? 0 : this.v2.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        Combo2<?, ?> other = (Combo2<?, ?>) obj;
        if (this.v1 == null) {
            if (other.v1 != null) {
                return false;
            }
        } else if (!this.v1.equals(other.v1)) {
            return false;
        }

        if (this.v2 == null) {
            if (other.v2 != null) {
                return false;
            }
        } else if (!this.v2.equals(other.v2)) {
            return false;
        }

        return true;
    }
}
